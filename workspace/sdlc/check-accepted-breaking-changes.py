#!/usr/bin/env python3
"""Fail on stale entries in the Owner-accepted OpenAPI breaking-change list (issue #1315).

Usage: check-accepted-breaking-changes.py BASE_SPEC REVISION_SPEC [LIST]

LIST defaults to backend-api/openapi/accepted-breaking-changes.txt. The oasdiff binary is
taken from $OASDIFF, else from PATH.

An entry ("<METHOD> <path> <change text>", as oasdiff prints it) only does something while
the break it accepts is between the base spec and the revision. Once that break is on main,
the base already contains it: the entry matches nothing and would silently accept a later
break with the same text. So every entry must match a breaking change oasdiff reports now,
with no ignore list, using the same rule as oasdiff err-ignore: the entry contains, case
insensitively, the operation and path, and the change text.

Exit codes: 0 every entry is live (or the list has none), 1 stale entries, 2 usage or
oasdiff error.
"""

from __future__ import annotations

import json
import os
import shutil
import subprocess
import sys

DEFAULT_LIST = "backend-api/openapi/accepted-breaking-changes.txt"


def entries(path: str) -> list[str]:
    with open(path, encoding="utf-8") as f:
        return [line.strip() for line in f if line.strip() and not line.lstrip().startswith("#")]


def breaking_changes(oasdiff: str, base: str, revision: str) -> list[dict]:
    result = subprocess.run(
        [oasdiff, "breaking", base, revision, "--format", "json"],
        capture_output=True,
        text=True,
        check=False,
    )
    if result.returncode != 0:
        raise RuntimeError(f"oasdiff exited {result.returncode}: {result.stderr.strip()}")
    return json.loads(result.stdout or "[]")


def is_live(entry: str, changes: list[dict]) -> bool:
    line = entry.lower()
    for change in changes:
        operation = f"{change.get('operation', '')} {change.get('path', '')}".lower()
        if operation in line and str(change.get("text", "")).lower() in line:
            return True
    return False


def main(argv: list[str]) -> int:
    if len(argv) not in (3, 4):
        print(__doc__.strip().splitlines()[2], file=sys.stderr)
        return 2
    base, revision = argv[1], argv[2]
    listing = argv[3] if len(argv) == 4 else DEFAULT_LIST
    oasdiff = os.environ.get("OASDIFF") or shutil.which("oasdiff")
    if not oasdiff:
        print("ERROR: oasdiff not found (set OASDIFF or put it on PATH)", file=sys.stderr)
        return 2

    accepted = entries(listing)
    if not accepted:
        print(f"OK: {listing} has no entries")
        return 0
    try:
        changes = breaking_changes(oasdiff, base, revision)
    except (RuntimeError, json.JSONDecodeError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 2

    stale = [entry for entry in accepted if not is_live(entry, changes)]
    if stale:
        print(f"FAIL: {len(stale)} stale entr{'y' if len(stale) == 1 else 'ies'} in {listing}.")
        print("Their break is already in the base spec, so they accept nothing now and would")
        print("hide a later break with the same text. Remove them (and their comment):")
        for entry in stale:
            print(f"  {entry}")
        return 1
    print(f"OK: {len(accepted)} accepted entr{'y' if len(accepted) == 1 else 'ies'}, all still breaking against the base")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
