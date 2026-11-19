"""Whether the published index matches the worktree's git HEAD. Standard library only."""

from __future__ import annotations

import subprocess
from pathlib import Path

from .paths import git_root


def git_head(root: Path) -> str:
    try:
        return subprocess.run(
            ["git", "-C", str(root), "rev-parse", "HEAD"],
            capture_output=True, check=True, timeout=30,
        ).stdout.decode().strip()
    except (OSError, subprocess.SubprocessError):
        return ""


def status(store, start: Path | None = None) -> tuple[bool, str, str]:
    """`(stale, indexed_commit, head)`. A store with no recorded commit is stale once HEAD is known.
    Outside a git work tree the index is not stale."""
    root = git_root(start or Path(getattr(store, "path", "") or "."))
    if root is None:
        return False, "", ""
    head = git_head(root)
    indexed = store.get_meta("indexed_head") or ""
    if isinstance(indexed, bytes):
        indexed = indexed.decode()
    return bool(head) and indexed != head, indexed, head


def stamp(store, start: Path | None = None) -> None:
    """Remember the commit this index describes, so a later query can see that HEAD moved."""
    root = git_root(start or Path(getattr(store, "path", "") or "."))
    if root is None:
        return
    head = git_head(root)
    if head:
        store.set_meta("indexed_head", head)
