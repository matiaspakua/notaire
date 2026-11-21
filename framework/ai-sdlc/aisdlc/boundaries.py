"""Extraction boundaries of the factory kit (ADR-006).

The marker is assembled so this file does not itself contain an adopter name. The kit core and the optional
dashboard name no adopter; the core imports no RepoLens. RepoLens checks its own side in its own tests.
"""
from __future__ import annotations

import re
from pathlib import Path

ADOPTER = re.compile("|".join(["notai" + "re", "backend" + "-api", "workspace" + "/sdlc"]), re.IGNORECASE)
IMPORT_REPOLENS = re.compile(r"^\s*(?:import|from)\s+repolens\b", re.MULTILINE)
PARENT_ESCAPE = re.compile(r"\.parents\[\s*([3-9]|\d{2,})\s*\]")

FACTORY_CORE = ("aisdlc", "bin", "templates", "plugins", "profiles")
DASHBOARD = ("factory-dashboard/src", "factory-dashboard/tests")


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


def scan_dashboard(kit: Path) -> list[str]:
    """Adopter names in the optional dashboard: it serves any project, so it names none."""
    hits: list[str] = []
    for path in _files(kit, DASHBOARD, (".ts", ".tsx", ".json", ".css", ".html", ".sh")):
        hits.extend(_hits(path, kit, path.read_text(encoding="utf-8", errors="replace"), (ADOPTER,)))
    return hits
