#!/usr/bin/env python3
"""Fail when a PR uses Closes/Fixes for a multi_pr or Owner-pending OpenSpec change.

CONSTITUTION §4 / #1878: multi-step PM work must use ``Refs #N`` on every PR except
the last. Process Checks invoke this on pull_request events.

Usage:
  python3 workspace/sdlc/check-closes-refs-guard.py
  python3 workspace/sdlc/check-closes-refs-guard.py --body-file /tmp/pr.md --base-sha SHA
"""

from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
from pathlib import Path

CLOSES = re.compile(
    r"(?im)\b(?:close[sd]?|fix(?:e[sd])?|resolve[sd]?)\s+#(\d+)\b"
)
REFS_ONLY = re.compile(r"(?im)\brefs?\s+#(\d+)\b")
OWNER_PENDING = re.compile(r"(?im)\bowner-pending\b|owner step pending")


def repo_root() -> Path:
    return Path(__file__).resolve().parents[2]


def is_multi_pr(change_dir: Path) -> bool:
    meta = change_dir / ".openspec.yaml"
    if not meta.is_file():
        return False
    return any(
        line.strip() == "multi_pr: true"
        for line in meta.read_text(encoding="utf-8").splitlines()
    )


def is_owner_pending(change_dir: Path) -> bool:
    for name in ("tasks.md", "proposal.md", "design.md", "traceability.md"):
        path = change_dir / name
        if path.is_file() and OWNER_PENDING.search(path.read_text(encoding="utf-8")):
            return True
    return False


def issue_numbers_for_change(change_dir: Path) -> set[int]:
    found: set[int] = set()
    for name in ("proposal.md", "traceability.md", "tasks.md"):
        path = change_dir / name
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8")
        for match in re.finditer(r"#(\d{3,})", text):
            found.add(int(match.group(1)))
        for match in re.finditer(r"Issue\s*\|\s*#(\d+)", text, re.I):
            found.add(int(match.group(1)))
    return found


def guarded_issue_numbers(changes_root: Path) -> dict[int, str]:
    """Map issue number -> reason when Closes/Fixes must not be used yet."""
    guarded: dict[int, str] = {}
    if not changes_root.is_dir():
        return guarded
    for change_dir in sorted(changes_root.iterdir()):
        if not change_dir.is_dir() or change_dir.name == "archive":
            continue
        if not (is_multi_pr(change_dir) or is_owner_pending(change_dir)):
            continue
        reason = "multi_pr: true" if is_multi_pr(change_dir) else "Owner-pending"
        for number in issue_numbers_for_change(change_dir):
            guarded[number] = f"{change_dir.name} ({reason})"
    return guarded


def closing_numbers(*texts: str) -> set[int]:
    found: set[int] = set()
    for text in texts:
        if not text:
            continue
        for match in CLOSES.finditer(text):
            found.add(int(match.group(1)))
    return found


def commit_messages(base_sha: str) -> list[str]:
    if not base_sha:
        return []
    try:
        out = subprocess.check_output(
            ["git", "log", "--format=%B", f"{base_sha}..HEAD"],
            text=True,
            stderr=subprocess.DEVNULL,
        )
    except subprocess.CalledProcessError:
        return []
    return [chunk for chunk in out.split("\n\n") if chunk.strip()]


def check(
    *,
    body: str,
    base_sha: str = "",
    changes_root: Path | None = None,
) -> list[str]:
    changes_root = changes_root or (repo_root() / "docs" / "openspec" / "changes")
    guarded = guarded_issue_numbers(changes_root)
    if not guarded:
        return []
    texts = [body, *commit_messages(base_sha)]
    hits = closing_numbers(*texts)
    errors: list[str] = []
    for number in sorted(hits & set(guarded)):
        errors.append(
            f"Closes/Fixes #{number} is forbidden while {guarded[number]} "
            f"— use Refs #{number} until the final PR"
        )
    return errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--body-file", help="PR body file (default: gh pr view)")
    parser.add_argument("--base-sha", default=os.environ.get("BASE_SHA", ""))
    parser.add_argument(
        "--body",
        default=os.environ.get("PR_BODY", ""),
        help="PR body text (tests / CI)",
    )
    args = parser.parse_args(argv)
    body = args.body
    if args.body_file:
        body = Path(args.body_file).read_text(encoding="utf-8")
    elif not body and os.environ.get("GITHUB_EVENT_PATH"):
        # Best-effort: leave empty; commit scan still runs.
        body = body or ""
    errors = check(body=body, base_sha=args.base_sha)
    if errors:
        print("✗ Closes/Fixes guard failed (#1878)")
        for err in errors:
            print(f"  {err}")
        return 1
    print("✓ Closes/Fixes guard OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
