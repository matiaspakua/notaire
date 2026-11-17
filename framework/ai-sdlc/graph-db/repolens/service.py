"""Application facade shared by the CLI, the HTTP API and the MCP server."""

from __future__ import annotations

import threading
from pathlib import Path

from .config import Config, Source, load_config
from .engine import Engine
from .indexer import index
from .store import Store


class KnowledgeGraph:
    def __init__(self, cfg: Config):
        self.cfg = cfg
        self._store: Store | None = None
        self._engine: Engine | None = None
        self._lock = threading.RLock()

    @classmethod
    def open(cls, config: Path | None = None, db: Path | None = None) -> KnowledgeGraph:
        if config is None and Path("repolens.toml").exists():
            config = Path("repolens.toml")
        return cls(load_config(config, db))

    @property
    def store(self) -> Store:
        if self._store is None:
            self._store = Store(self.cfg.db_path)
        return self._store

    @property
    def engine(self) -> Engine:
        with self._lock:
            if self._engine is None:
                self._engine = Engine(self.store, self.cfg)
            else:
                self._engine.refresh()
            return self._engine

    def add_paths(self, paths: list[str], name: str | None = None) -> list[str]:
        names = []
        for raw in paths:
            path = Path(raw).expanduser().resolve()
            src_name = name if (name and len(paths) == 1) else path.name
            existing = next((s for s in self.cfg.sources if s.name == src_name), None)
            if existing is None:
                self.cfg.sources.append(Source(src_name, path))
            names.append(src_name)
        return names

    def sources_from_store(self) -> None:
        known = {s.name for s in self.cfg.sources}
        for project in self.store.projects():
            if project["name"] not in known:
                self.cfg.sources.append(Source(project["name"], Path(project["root"])))

    def index(self, only: list[str] | None = None, full: bool = False, embed: bool = True, log=print) -> dict:
        with self._lock:
            if not self.cfg.sources:
                self.sources_from_store()
            if not self.cfg.sources:
                raise ValueError("nothing to index: pass folders or configure [[source]] entries")
            report = index(self.cfg, self.store, only=only, full=full, embed=embed, log=log)
            if self._engine is not None:
                self._engine.load()
            return report

    def close(self) -> None:
        if self._store is not None:
            self._store.close()
