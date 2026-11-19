"""Fill an empty worktree store from another checkout of the same git directory.

A fresh worktree has its own `.repolens/` and would otherwise answer `no results` for every
query. When another worktree of the same common git dir already has files in its store, copy
that store. Callers that still have nothing to search must say so and start an index, instead
of reporting a miss.
"""
from __future__ import annotations

import sqlite3
import subprocess
from pathlib import Path

from .paths import STATE_DIR, git_root

EMPTY_INDEX = (
    "repolens: this worktree has no index yet (0 files). "
    "Indexing started in the background (log: .repolens/refresh.log). "
    "Retry this search when it finishes. For an exact symbol or path, use rg."
)


def _git(root: Path, *args: str) -> str:
    try:
        done = subprocess.run(
            ["git", "-C", str(root), *args], capture_output=True, text=True, timeout=15, check=False,
        )
    except (OSError, subprocess.SubprocessError):
        return ""
    return done.stdout.strip() if done.returncode == 0 else ""


def common_dir(root: Path) -> str:
    found = _git(root, "rev-parse", "--path-format=absolute", "--git-common-dir")
    return found or str(root)


def worktrees(root: Path) -> list[Path]:
    text = _git(root, "worktree", "list", "--porcelain")
    return [Path(line.split(" ", 1)[1]) for line in text.splitlines() if line.startswith("worktree ")]


def _file_count(db: Path) -> int:
    try:
        conn = sqlite3.connect(f"file:{db}?mode=ro", uri=True)
    except sqlite3.Error:
        return 0
    try:
        row = conn.execute("SELECT COUNT(*) FROM files").fetchone()
    except sqlite3.Error:
        return 0
    finally:
        conn.close()
    return int(row[0]) if row else 0


def _indexed_head(db: Path) -> str:
    try:
        conn = sqlite3.connect(f"file:{db}?mode=ro", uri=True)
        row = conn.execute("SELECT value FROM meta WHERE key = 'indexed_head'").fetchone()
        conn.close()
    except sqlite3.Error:
        return ""
    if not row or row[0] is None:
        return ""
    return row[0].decode() if isinstance(row[0], bytes) else str(row[0])


def candidates(root: Path, own: Path) -> list[Path]:
    """Non-empty stores for this git directory, same HEAD first."""
    identity = common_dir(root)
    head = _git(root, "rev-parse", "HEAD")
    found: list[tuple[bool, float, Path]] = []
    seen: set[Path] = set()

    def add(db: Path) -> None:
        db = db.resolve()
        if db in seen or db == own.resolve() or not db.is_file():
            return
        count = _file_count(db)
        if count <= 0:
            return
        seen.add(db)
        same = bool(head) and _indexed_head(db) == head
        found.append((same, db.stat().st_mtime, db))

    try:
        from . import repos

        for entry in repos.listed():
            if entry.get("repo") != identity:
                continue
            db = entry.get("db")
            if db:
                add(Path(db))
    except (OSError, ValueError):
        pass
    for tree in worktrees(root):
        if tree.resolve() == root.resolve():
            continue
        add(tree / STATE_DIR / "graph.db")
    found.sort(key=lambda item: (item[0], item[1]), reverse=True)
    return [db for _same, _mtime, db in found]


def copy_db(src: Path, dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    for suffix in ("", "-wal", "-shm"):
        extra = Path(str(dest) + suffix) if suffix else dest
        if suffix and extra.is_file():
            extra.unlink()
    if dest.exists():
        dest.unlink()
    source = sqlite3.connect(f"file:{src}?mode=ro", uri=True)
    target = sqlite3.connect(dest)
    try:
        source.backup(target)
    finally:
        source.close()
        target.close()


def adopt(kg) -> str | None:
    """Copy a sibling store into this empty one. None when there is nothing to copy."""
    own = Path(kg.cfg.db_path)
    if kg.store.file_signatures_count():
        return None
    root = git_root(own if own.is_absolute() else Path.cwd())
    if root is None:
        return None
    donor = next(iter(candidates(root, own)), None)
    if donor is None:
        return None
    kg.release()
    copy_db(donor, own)
    return f"repolens: bootstrapped this worktree index from {donor}"
