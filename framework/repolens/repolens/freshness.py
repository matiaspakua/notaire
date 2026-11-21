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


def status(store, start: Path | None = None, extractors: str | None = None) -> tuple[bool, str, str]:
    """`(stale, indexed_commit, head)`. A store with no recorded commit is stale once HEAD is known, and so is
    one written by other `extractors` than the running code's: a branch switch can leave the index of another
    checkout's code stamped with the current HEAD. Outside a git work tree only the extractors count."""
    if extractors is not None and outdated(store, extractors):
        return True, "extractors " + str(store.get_meta("extractor_version") or "unknown"), "extractors " + extractors
    root = git_root(start or Path(getattr(store, "path", "") or "."))
    if root is None:
        return False, "", ""
    head = git_head(root)
    indexed = store.get_meta("indexed_head") or ""
    if isinstance(indexed, bytes):
        indexed = indexed.decode()
    return bool(head) and indexed != head, indexed, head


def outdated(store, extractors: str) -> bool:
    """The store holds an index written by older extractors than `extractors` (a newer one is left alone:
    see `newer`)."""
    found = _version(store)
    return bool(found) and found != extractors and not newer(found, extractors)


def newer(found: str, extractors: str) -> bool:
    """`found` is a later extractor version than the running code's: a process started before an upgrade
    (a long-lived MCP server) must not rewrite that index with its older extractors."""
    return found.isdigit() and extractors.isdigit() and int(found) > int(extractors)


def _version(store) -> str:
    found = store.get_meta("extractor_version") or ""
    return found.decode() if isinstance(found, bytes) else str(found)


def stamp(store, start: Path | None = None) -> None:
    """Remember the commit this index describes, so a later query can see that HEAD moved."""
    root = git_root(start or Path(getattr(store, "path", "") or "."))
    if root is None:
        return
    head = git_head(root)
    if head:
        store.set_meta("indexed_head", head)
