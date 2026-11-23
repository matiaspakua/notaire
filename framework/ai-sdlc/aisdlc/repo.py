"""Read-only git queries and path globbing. The kit never writes to git."""
from __future__ import annotations

import fnmatch
import os
import subprocess
from pathlib import Path

# Git hooks export these; inherited, they would point every query at the hook's repository.
LOCATION_VARIABLES = ("GIT_DIR", "GIT_WORK_TREE", "GIT_INDEX_FILE", "GIT_COMMON_DIR", "GIT_PREFIX")


class GitError(Exception):
    """A git query failed (missing ref, not a repository)."""


def clean_env() -> dict[str, str]:
    return {k: v for k, v in os.environ.items() if k not in LOCATION_VARIABLES}


def _git(root: Path, *args: str) -> subprocess.CompletedProcess:
    return subprocess.run(["git", *args], cwd=root, capture_output=True, text=True, env=clean_env())


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


def numstat(root: Path, base: str) -> list[tuple[int | None, int | None, str]]:
    """(added, deleted, path) per changed file since the merge base; None for binary files.

    Uses ``-M`` to detect renames so a pure rename contributes 0 lines and a rename
    with edits contributes only the edited lines.  The path reported (and matched
    against [size] exclude) is the **new** path.
    """
    require_ref(root, base)
    # With -M -z, each record is NUL-terminated.
    # Normal file: "added\tdeleted\tpath\x00"
    # Rename:      "added\tdeleted\t\x00old_path\x00new_path\x00"
    # (the third tab field is empty, then NUL-separated old and new paths)
    result = _git(root, "diff", "--numstat", "-M", "-z", f"{base}...HEAD")
    if result.returncode != 0:
        raise GitError(result.stderr.strip())
    rows: list[tuple[int | None, int | None, str]] = []
    raw = result.stdout
    if not raw:
        return rows
    # Split on NUL to get all fields; reconstruct records.
    # Each record starts with "added\tdeleted\t" then either:
    #   - path\x00  (normal)
    #   - \x00old_path\x00new_path\x00  (rename)
    tokens = raw.split("\x00")
    i = 0
    while i < len(tokens):
        # Each record begins with "added\tdeleted\tpath" or "added\tdeleted\t"
        head = tokens[i]
        if not head.strip():
            i += 1
            continue
        parts = head.split("\t")
        if len(parts) == 3 and parts[2]:
            # Normal file: added, deleted, path
            added, deleted, path = parts
            rows.append((
                None if added == "-" else int(added),
                None if deleted == "-" else int(deleted),
                path,
            ))
            i += 1
        elif len(parts) == 3 and parts[2] == "":
            # Rename: added, deleted, empty — next two tokens are old_path, new_path
            added, deleted, _ = parts
            old_path = tokens[i + 1] if i + 1 < len(tokens) else ""
            new_path = tokens[i + 2] if i + 2 < len(tokens) else ""
            rows.append((
                None if added == "-" else int(added),
                None if deleted == "-" else int(deleted),
                new_path,
            ))
            i += 3
        else:
            i += 1
    return rows


def subjects(root: Path, base: str) -> list[str]:
    require_ref(root, base)
    result = _git(root, "log", "--no-merges", "--format=%s", f"{base}..HEAD")
    if result.returncode != 0:
        raise GitError(result.stderr.strip())
    return [line for line in result.stdout.splitlines() if line]


def commits_since(root: Path, base: str) -> list[dict[str, str]]:
    """Non-merge commits since base: sha, author_name, author_email, message (subject+body)."""
    require_ref(root, base)
    # Record separator 0x1e; field separator 0x00 — message may contain newlines.
    result = _git(
        root,
        "log",
        "--no-merges",
        "--format=%H%x00%an%x00%ae%x00%B%x1e",
        f"{base}..HEAD",
    )
    if result.returncode != 0:
        raise GitError(result.stderr.strip())
    rows: list[dict[str, str]] = []
    for record in result.stdout.split("\x1e"):
        record = record.strip("\n")
        if not record.strip():
            continue
        parts = record.split("\x00", 3)
        if len(parts) != 4:
            continue
        sha, name, email, message = parts
        rows.append(
            {
                "sha": sha.strip(),
                "author_name": name,
                "author_email": email,
                "message": message.strip("\n"),
            }
        )
    return rows


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
