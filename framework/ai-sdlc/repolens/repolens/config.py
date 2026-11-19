"""Configuration: one TOML file, or defaults built from CLI paths."""

from __future__ import annotations

import os
import tomllib
from dataclasses import dataclass, field
from pathlib import Path

from .paths import STATE_DIR, find_config, project_root

DEFAULT_EXCLUDES = [
    ".git/**", "**/.git/**", "**/node_modules/**", "**/.venv/**", "**/venv/**", "**/__pycache__/**",
    "**/target/**", "**/dist/**", "**/build/**", "**/.next/**", "**/coverage/**", "**/.repolens/**",
    "**/.pytest_cache/**", "**/.mypy_cache/**", "**/.ruff_cache/**", "**/*.min.js", "**/*.map",
    "**/package-lock.json", "**/yarn.lock", "**/pnpm-lock.yaml", "**/*.lock",
]

DEFAULT_TAGS = {
    "issue": r"(?<![\w&#/=])#(\d{2,6})\b",
    "adr": r"\bADR-(\d{1,4})\b",
}

DEFAULT_EDGE_WEIGHTS = {
    "contains": 0.5,
    "dir_contains": 0.12,
    "imports": 0.7,
    "calls": 0.8,
    "references": 0.6,
    "inherits": 1.0,
    "tests": 1.0,
    "http_call": 2.0,
    "handles": 2.0,
    "defines_table": 0.8,
    "uses_table": 0.8,
    "co_change": 0.5,
    "links": 0.6,
    "mentions": 0.5,
    "tagged": 0.35,
    "changed_for": 0.3,
    "depends_on": 0.1,
}


@dataclass
class Source:
    name: str
    path: Path
    include: list[str] = field(default_factory=list)
    exclude: list[str] = field(default_factory=list)


@dataclass
class Config:
    db_path: Path
    sources: list[Source] = field(default_factory=list)
    exclude: list[str] = field(default_factory=lambda: list(DEFAULT_EXCLUDES))
    max_file_bytes: int = 1_000_000
    max_pdf_bytes: int = 30_000_000
    workers: int = max(1, (os.cpu_count() or 2) - 1)
    embedder: str = "auto"
    embed_model: str = "BAAI/bge-small-en-v1.5"
    embed_dim: int = 256
    embed_reduce: int = 128
    embed_workers: int = 0
    embed_skip: list[str] = field(default_factory=list)
    git_commits: int = 3000
    cochange_max_files: int = 30
    cochange_min_count: int = 2
    tags: dict[str, str] = field(default_factory=lambda: dict(DEFAULT_TAGS))
    route_prefixes: list[str] = field(default_factory=lambda: ["/api/v1", "/api"])
    edge_weights: dict[str, float] = field(default_factory=lambda: dict(DEFAULT_EDGE_WEIGHTS))
    reverse_weight: float = 0.7
    tuning: dict[str, float] = field(default_factory=dict)
    demote: list[str] = field(default_factory=list)
    demote_factor: float = 0.35

    def source(self, name: str) -> Source:
        for src in self.sources:
            if src.name == name:
                return src
        raise KeyError(f"unknown source {name!r}")


def default_db_path() -> Path:
    return Path(os.environ.get("REPOLENS_DB", ".repolens/graph.db"))


def load_config(path: Path | None = None, db: Path | None = None) -> Config:
    """Load a TOML config; relative paths resolve against the config file's folder. Without a config path,
    `$REPOLENS_CONFIG` or the nearest repolens.toml is used; without any, the repository itself is indexed
    (zero configuration). An explicit `db` (or `$REPOLENS_DB`) without a config opens just that store."""
    if path is None:
        env = os.environ.get("REPOLENS_CONFIG")
        path = Path(env) if env else None
    if path is None and db is None and not os.environ.get("REPOLENS_DB"):
        path = find_config()
        if path is None:
            return default_config()
    if path is None:
        return Config(db_path=db or default_db_path())
    data = tomllib.loads(Path(path).read_text(encoding="utf-8"))
    base = Path(path).resolve().parent
    return config_from_dict(data, base, db)


def default_config(start: Path | None = None) -> Config:
    """No repolens.toml: index the repository (or folder) and keep the store in its `.repolens/` folder."""
    root = project_root(start)
    cfg = Config(db_path=root / STATE_DIR / "graph.db")
    cfg.sources.append(Source(root.name, root))
    return cfg


def config_from_dict(data: dict, base: Path, db: Path | None = None) -> Config:
    index = data.get("index", {})
    cfg = Config(db_path=db or _resolve(base, index.get("db", ".repolens/graph.db")))
    for key in ("max_file_bytes", "max_pdf_bytes", "workers"):
        if key in index:
            setattr(cfg, key, int(index[key]))
    cfg.exclude = list(DEFAULT_EXCLUDES) + list(index.get("exclude", []))
    for raw in data.get("source", []):
        cfg.sources.append(Source(
            name=raw["name"],
            path=_resolve(base, raw["path"]),
            include=list(raw.get("include", [])),
            exclude=list(raw.get("exclude", [])),
        ))
    embed = data.get("embed", {})
    cfg.embedder = embed.get("backend", cfg.embedder)
    cfg.embed_model = embed.get("model", cfg.embed_model)
    cfg.embed_dim = int(embed.get("dim", cfg.embed_dim))
    cfg.embed_reduce = int(embed.get("reduce", cfg.embed_reduce))
    cfg.embed_workers = int(embed.get("workers", cfg.embed_workers))
    cfg.embed_skip = list(embed.get("skip", cfg.embed_skip))
    git = data.get("git", {})
    cfg.git_commits = int(git.get("commits", cfg.git_commits))
    cfg.cochange_max_files = int(git.get("max_files_per_commit", cfg.cochange_max_files))
    cfg.cochange_min_count = int(git.get("min_count", cfg.cochange_min_count))
    if "tags" in data:
        cfg.tags = {**cfg.tags, **data["tags"]}
    routes = data.get("routes", {})
    cfg.route_prefixes = list(routes.get("prefixes", cfg.route_prefixes))
    ranking = data.get("ranking", {})
    cfg.edge_weights = {**cfg.edge_weights, **ranking.get("weights", {})}
    cfg.reverse_weight = float(ranking.get("reverse_weight", cfg.reverse_weight))
    cfg.tuning = {k: float(v) for k, v in ranking.get("tuning", {}).items()}
    if "restart" in ranking:
        cfg.tuning["restart"] = float(ranking["restart"])
    cfg.demote = list(ranking.get("demote", cfg.demote))
    cfg.demote_factor = float(ranking.get("demote_factor", cfg.demote_factor))
    return cfg


def _resolve(base: Path, value: str) -> Path:
    p = Path(os.path.expanduser(value))
    return p if p.is_absolute() else (base / p).resolve()
