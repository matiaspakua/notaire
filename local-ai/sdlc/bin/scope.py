#!/usr/bin/env python3
"""Build a phase's allowed-path regex (ERE, for grep -E and the pre-commit hook).

  scope.py implement <worktree> <change> <source_roots> <triage.md> <traceability.md>

The implement scope is .localai/, the change's OpenSpec folder, the source roots, and, as exact
paths, every existing file the change plans: triage "## Files to Edit" and traceability "## Planned Files".
"""
import os
import re
import sys

ERE_SPECIAL = re.compile(r"([.\[\]()*+?{}|^$\\])")


def _section(text, heading):
    match = re.search(r"^## %s\s*$(.*?)(?=^## |\Z)" % re.escape(heading), text, re.M | re.S)
    return match.group(1) if match else ""


def _candidates(triage, traceability):
    for line in _section(triage, "Files to Edit").splitlines():
        words = line[2:].replace("`", "").split() if line.startswith("- ") else []
        if words:
            yield words[0]
    for line in _section(traceability, "Planned Files").splitlines():
        if line.startswith("|"):
            yield line.strip("|").split("|")[0].strip().strip("`")


def planned_files(worktree, triage, traceability):
    paths = []
    for path in _candidates(triage, traceability):
        if path and path not in paths and os.path.isfile(os.path.join(worktree, path)):
            paths.append(path)
    return paths


def implement_scope(worktree, change, source_roots, triage, traceability):
    parts = [r"^(\.localai/|openspec/changes/%s/)" % ERE_SPECIAL.sub(r"\\\1", change), source_roots]
    parts += ["^%s$" % ERE_SPECIAL.sub(r"\\\1", p) for p in planned_files(worktree, triage, traceability)]
    return "|".join(parts)


def _read(path):
    try:
        with open(path) as f:
            return f.read()
    except FileNotFoundError:
        return ""


def main(argv):
    if len(argv) != 6 or argv[0] != "implement":
        print(__doc__, file=sys.stderr)
        return 2
    _, worktree, change, source_roots, triage, traceability = argv
    print(implement_scope(worktree, change, source_roots, _read(triage), _read(traceability)))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
