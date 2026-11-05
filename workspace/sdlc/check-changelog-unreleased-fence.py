#!/usr/bin/env python3
"""Fail when more than one open PR edits CHANGELOG.md [Unreleased] (#1693 fence).

Fleet lesson: parallel Unreleased writers cause conflict storms and extra CI
rounds. Process Checks / preflight can call this with ``gh`` available.
"""

from __future__ import annotations

import json
import subprocess
import sys


def _gh_json(args: list[str]) -> object:
    proc = subprocess.run(
        ["gh", *args],
        capture_output=True,
        text=True,
        check=False,
    )
    if proc.returncode != 0:
        raise SystemExit(f"gh {' '.join(args)} failed: {proc.stderr.strip()}")
    return json.loads(proc.stdout or "null")


def main() -> int:
    current = subprocess.run(
        ["gh", "pr", "view", "--json", "number,files"],
        capture_output=True,
        text=True,
        check=False,
    )
    # Not on a PR tip — advisory skip (local branch without PR).
    if current.returncode != 0:
        print("check-changelog-unreleased-fence: no current PR — skip")
        return 0

    here = json.loads(current.stdout)
    here_n = here.get("number")
    here_files = {f.get("path") for f in here.get("files") or []}
    if "CHANGELOG.md" not in here_files:
        print(f"PR #{here_n} does not touch CHANGELOG.md — fence n/a")
        return 0

    others = _gh_json(
        [
            "pr",
            "list",
            "--state",
            "open",
            "--limit",
            "50",
            "--json",
            "number,title,files",
        ]
    )
    assert isinstance(others, list)
    writers = []
    for pr in others:
        num = pr.get("number")
        if num == here_n:
            continue
        paths = {f.get("path") for f in pr.get("files") or []}
        if "CHANGELOG.md" in paths:
            writers.append(f"#{num} {pr.get('title', '')}".strip())

    if writers:
        print(
            "CHANGELOG [Unreleased] fence: this PR edits CHANGELOG.md while "
            "other open PRs also do:\n  - " + "\n  - ".join(writers),
            file=sys.stderr,
        )
        print(
            "Hold Unreleased to one writer (FLEET-LESSONS / CI-MERGE-GATE).",
            file=sys.stderr,
        )
        return 1

    print(f"PR #{here_n} is the sole open CHANGELOG.md writer — fence OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
