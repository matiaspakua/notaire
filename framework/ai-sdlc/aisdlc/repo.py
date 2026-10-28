"""Read-only git queries and path globbing. The kit never writes to git."""
from __future__ import annotations

import fnmatch
import subprocess
from pathlib import Path


class GitError(Exception):
    """A git query failed (missing ref, not a repository)."""


def _git(root: Path, *args: str) -> subprocess.CompletedProcess:
    return subprocess.run(["git", *args], cwd=root, capture_output=True, text=True)


def toplevel(cwd: Path) -> Path | None:
    result = _git(cwd, "rev-parse", "--show-toplevel")
    return Path(result.stdout.strip()) if result.returncode == 0 else None


def head(root: Path) -> str | None:
    result = _git(root, "rev-parse", "HEAD")
    return result.stdout.strip() if result.returncode == 0 else None


def ref_exists(root: Path, ref: str) -> bool:
    return _git(root, "rev-parse", "--verify", "--quiet", f"{ref}^{{commit}}").returncode == 0


def require_ref(root: Path, ref: str) -> None:
    if not ref_exists(root, ref):
        raise GitError(f"base revision {ref!r} does not exist (fetch it, or pass --base)")


def changed_files(root: Path, base: str) -> list[str]:
    require_ref(root, base)
    result = _git(root, "diff", "--name-only", f"{base}...HEAD")
    if result.returncode != 0:
        raise GitError(result.stderr.strip())
    return [line for line in result.stdout.splitlines() if line]


def subjects(root: Path, base: str) -> list[str]:
    require_ref(root, base)
    result = _git(root, "log", "--no-merges", "--format=%s", f"{base}..HEAD")
    if result.returncode != 0:
        raise GitError(result.stderr.strip())
    return [line for line in result.stdout.splitlines() if line]


def show(root: Path, ref: str, path: str) -> str | None:
    """The file's content at `ref`, or None when the file does not exist there."""
    require_ref(root, ref)
    result = _git(root, "show", f"{ref}:{path}")
    return result.stdout if result.returncode == 0 else None


def matches(path: str, patterns: tuple[str, ...] | list[str]) -> bool:
    """fnmatch where a leading `**/` also matches at the repository root."""
    for pattern in patterns:
        if fnmatch.fnmatchcase(path, pattern):
            return True
        if pattern.startswith("**/") and fnmatch.fnmatchcase(path, pattern[3:]):
            return True
    return False
