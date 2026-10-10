#!/usr/bin/env python3
"""Assemble and check the Owner-accepted OpenAPI breaking changes (issue #1315).

Usage: check-accepted-breaking-changes.py BASE_SPEC REVISION_SPEC [--dir DIR]
           [--base-ref REF] [--write-ignore OUT] [--prune]

DIR defaults to backend-api/openapi/accepted-breaking-changes.d and REF to origin/main.
The oasdiff binary is taken from $OASDIFF, else from PATH. Run it from the repository root.

Each pull request that introduces an intended break adds its own DIR/<issue>-<slug>.txt with
the lines oasdiff prints ("<METHOD> <path> <change text>"), each under a "# #<issue>" comment
saying why no working client breaks. One file per pull request means two open pull requests
never edit the same lines, so accepting a break no longer conflicts with every other branch.

- A file that exists unchanged at REF is "merged": its break is already in the base spec, so
  its entries are never ignored (they would only hide a later break with the same text). It
  is reported for deletion; --prune deletes it. Deleting it on several branches merges
  cleanly.
- Every other *.txt belongs to this pull request. Each entry must match a breaking change
  oasdiff reports between the base and the revision with no ignore list, using the oasdiff
  err-ignore rule (case-insensitive, METHOD + path and the change text); an entry matching
  nothing is stale (or a typo) and fails. The file name must start with the issue number.
- --write-ignore OUT writes this pull request's entries: the only list oasdiff may ignore.
  On main every file is merged, so the list is empty by default.
- The legacy single list backend-api/openapi/accepted-breaking-changes.txt must not exist.

Exit codes: 0 OK, 1 stale entries or invalid files, 2 usage, git or oasdiff error.
"""

from __future__ import annotations

import argparse
import glob
import json
import os
import re
import shutil
import subprocess
import sys

DEFAULT_DIR = "backend-api/openapi/accepted-breaking-changes.d"
LEGACY_LIST = "backend-api/openapi/accepted-breaking-changes.txt"
FILE_NAME = re.compile(r"^[0-9]+-[A-Za-z0-9._-]+\.txt$")
ENTRY_SHAPE = re.compile(r"^(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS) /\S* \S.*$", re.IGNORECASE)
ISSUE_REF = re.compile(r"#[0-9]+")


class GitError(RuntimeError):
    pass


def base_content(ref: str, path: str) -> bytes | None:
    """The file at REF, or None when it does not exist there."""
    if subprocess.run(["git", "rev-parse", "--verify", "--quiet", f"{ref}^{{commit}}"],
                      capture_output=True, check=False).returncode != 0:
        raise GitError(f"base ref {ref!r} not found (fetch it first)")
    result = subprocess.run(["git", "show", f"{ref}:{path}"], capture_output=True, check=False)
    return result.stdout if result.returncode == 0 else None


def parse(path: str) -> tuple[list[str], list[str]]:
    """Entries of one file and the problems in its shape."""
    entries, problems, issue_seen = [], [], False
    with open(path, encoding="utf-8") as f:
        for number, raw in enumerate(f.read().splitlines(), start=1):
            line = raw.strip()
            if not line:
                continue
            if line.startswith("#"):
                issue_seen = issue_seen or bool(ISSUE_REF.search(line))
                continue
            if not ENTRY_SHAPE.match(line):
                problems.append(f"line {number}: expected '<METHOD> <path> <oasdiff text>': {line}")
            elif not issue_seen:
                problems.append(f"line {number}: needs a '# #<issue>' comment above it: {line}")
            entries.append(line)
    return entries, problems


def breaking_changes(oasdiff: str, base: str, revision: str) -> list[dict]:
    result = subprocess.run(
        [oasdiff, "breaking", base, revision, "--format", "json"],
        capture_output=True, text=True, check=False,
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


def write_ignore(path: str, owned: dict[str, list[str]]) -> None:
    os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write("# Assembled by workspace/sdlc/check-accepted-breaking-changes.py (#1315).\n")
        f.write("# Entries of this pull request's accepted-breaking-changes.d files only.\n")
        for name, entries in owned.items():
            f.write(f"# {name}\n")
            f.writelines(f"{entry}\n" for entry in entries)


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(usage=__doc__.strip().splitlines()[2].removeprefix("Usage: "))
    parser.add_argument("base_spec")
    parser.add_argument("revision_spec")
    parser.add_argument("--dir", default=DEFAULT_DIR)
    parser.add_argument("--base-ref", default="origin/main")
    parser.add_argument("--write-ignore")
    parser.add_argument("--prune", action="store_true")
    try:
        args = parser.parse_args(argv[1:])
    except SystemExit:
        return 2

    failed = False
    if os.path.exists(LEGACY_LIST):
        print(f"FAIL: {LEGACY_LIST} is replaced by {args.dir}/ (#1315). Move this pull request's")
        print(f"entries to {args.dir}/<issue>-<slug>.txt and delete the old file.")
        failed = True

    owned: dict[str, list[str]] = {}
    merged: list[str] = []
    try:
        for path in sorted(glob.glob(os.path.join(args.dir, "*.txt"))):
            name = os.path.basename(path)
            rel = os.path.relpath(path).replace(os.sep, "/")
            with open(path, "rb") as f:
                if base_content(args.base_ref, rel) == f.read():
                    merged.append(path)
                    continue
            entries, problems = parse(path)
            if not FILE_NAME.match(name):
                problems.insert(0, "file name must be <issue>-<slug>.txt")
            for problem in problems:
                print(f"FAIL: {rel}: {problem}")
            failed = failed or bool(problems)
            owned[name] = entries
    except GitError as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 2

    if merged:
        verb = "Deleted" if args.prune else "Delete"
        print(f"NOTE: {len(merged)} file(s) unchanged on {args.base_ref} belong to merged pull requests;")
        print(f"their breaks are already in the base spec, so they are not ignored. {verb}:")
        for path in merged:
            print(f"  {os.path.relpath(path)}")
            if args.prune:
                os.remove(path)
        if not args.prune:
            print("  (run with --prune, or bash workspace/sdlc/preflight.sh --fix)")

    accepted = [(name, entry) for name, entries in owned.items() for entry in entries]
    if accepted:
        oasdiff = os.environ.get("OASDIFF") or shutil.which("oasdiff")
        if not oasdiff:
            print("ERROR: oasdiff not found (set OASDIFF or put it on PATH)", file=sys.stderr)
            return 2
        try:
            changes = breaking_changes(oasdiff, args.base_spec, args.revision_spec)
        except (RuntimeError, json.JSONDecodeError) as error:
            print(f"ERROR: {error}", file=sys.stderr)
            return 2
        stale = [(name, entry) for name, entry in accepted if not is_live(entry, changes)]
        if stale:
            print(f"FAIL: {len(stale)} stale entr{'y' if len(stale) == 1 else 'ies'}: no breaking change")
            print("between the base and this revision matches them (already on main, or a typo).")
            print("Remove them, or copy the exact line oasdiff prints:")
            for name, entry in stale:
                print(f"  {name}: {entry}")
            failed = True

    if args.write_ignore:
        write_ignore(args.write_ignore, owned)
    if failed:
        return 1
    print(f"OK: {len(accepted)} accepted entr{'y' if len(accepted) == 1 else 'ies'} from "
          f"{len(owned)} file(s) of this pull request, all still breaking against the base")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
