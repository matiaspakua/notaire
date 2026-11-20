"""Extraction boundaries for the factory kit and for RepoLens.

The marker is assembled so this file does not itself contain an adopter name.
Scans cover the installable cores and the runners, not published benchmark
tables or adopter task files.
"""
from __future__ import annotations

import re
from pathlib import Path

ADOPTER = re.compile("|".join(["notai" + "re", "backend" + "-api", "workspace" + "/sdlc"]), re.IGNORECASE)
IMPORT_REPOLENS = re.compile(r"^\s*(?:import|from)\s+repolens\b", re.MULTILINE)
IMPORT_AISDLC = re.compile(r"^\s*(?:import|from)\s+aisdlc\b", re.MULTILINE)
PARENT_ESCAPE = re.compile(r"\.parents\[\s*([3-9]|\d{2,})\s*\]")

FACTORY_CORE = ("aisdlc", "bin", "templates", "plugins", "profiles")
REPOLENS_CODE = ("repolens", "bench", "bin")
REPOLENS_SKIP = ("bench/results", "bench/tasks", "bench/callgraph")


def _hits(path: Path, root: Path, text: str, patterns: tuple[re.Pattern[str], ...]) -> list[str]:
    found = []
    for number, line in enumerate(text.splitlines(), 1):
        if any(pattern.search(line) for pattern in patterns):
            found.append(f"{path.relative_to(root)}:{number}: {line.strip()}")
    return found


def _files(root: Path, folders: tuple[str, ...], suffixes: tuple[str, ...], skip: tuple[str, ...] = ()) -> list[Path]:
    paths: list[Path] = []
    for folder in folders:
        base = root / folder
        if not base.exists():
            continue
        for path in sorted(base.rglob("*")):
            if not path.is_file() or "__pycache__" in path.parts:
                continue
            relative = path.relative_to(root).as_posix()
            if any(relative == item or relative.startswith(item + "/") for item in skip):
                continue
            if path.suffix in suffixes or path.name in suffixes:
                paths.append(path)
    return paths


def scan_factory(kit: Path) -> list[str]:
    """Adopter names, parent escapes, and a direct RepoLens import inside the factory core."""
    hits: list[str] = []
    for path in _files(kit, FACTORY_CORE, (".py", ".sh", ".toml", ".yml", ".yaml", ".md")):
        text = path.read_text(encoding="utf-8", errors="replace")
        hits.extend(_hits(path, kit, text, (ADOPTER, IMPORT_REPOLENS, PARENT_ESCAPE)))
    return hits


def scan_repolens(root: Path) -> list[str]:
    """Adopter names, parent escapes, and a factory import inside the RepoLens package and runners."""
    hits: list[str] = []
    files = _files(root, REPOLENS_CODE, (".py", ".sh", ".toml"), REPOLENS_SKIP)
    project = root / "pyproject.toml"
    if project.is_file():
        files.append(project)
    for path in files:
        text = path.read_text(encoding="utf-8", errors="replace")
        hits.extend(_hits(path, root, text, (ADOPTER, IMPORT_AISDLC, PARENT_ESCAPE)))
    return hits
