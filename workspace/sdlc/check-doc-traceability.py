#!/usr/bin/env python3
"""A change that names permanent docs in traceability.md must change those files.

CONSTITUTION §8: an implementation validates the documents its own traceability
names (use case, lifecycle, SAD, ADR, diagram). The ledger is
`docs/openspec/changes/<name>/traceability.md`, section
"Permanent documentation updated". A row marked yes is a claim. The claimed
path has to be in the same diff.

USAGE
  python3 workspace/sdlc/check-doc-traceability.py <base> [head]
"""

from __future__ import annotations

import re
import subprocess
import sys

SECTION = re.compile(r"^## Permanent documentation updated\s*$", re.M)
NEXT = re.compile(r"^## ", re.M)
ROW = re.compile(r"^\|(.+)\|\s*$")
YES = {"yes", "sí", "si"}


def git(args: list[str]) -> str:
    # core.quotepath=false: paths with en-dashes (e.g. CU76 – …) must match
    # traceability claims literally, not as C-quoted "\342\200\223" forms.
    result = subprocess.run(
        ["git", "-c", "core.quotepath=false", *args],
        check=True,
        capture_output=True,
        text=True,
    )
    return result.stdout


def diff_base(base: str, head: str) -> str:
    """On a pull-request merge checkout, diff from the merge parent."""
    parents = subprocess.run(
        ["git", "rev-parse", "--verify", "--quiet", f"{head}^2"],
        capture_output=True,
        text=True,
    )
    if parents.returncode == 0:
        base = git(["rev-parse", f"{head}^1"]).strip()
    return base


def permanent_rows(text: str) -> list[tuple[str, str]] | None:
    """Rows under Permanent documentation updated.

    None when the section is missing. An empty list means the section has no
    data rows. A pending row is kept so a filename citation can land in an
    open ledger without pretending a permanent document was updated.
    """
    match = SECTION.search(text)
    if not match:
        return None
    rest = text[match.end() :]
    nxt = NEXT.search(rest)
    body = rest[: nxt.start()] if nxt else rest
    rows: list[tuple[str, str]] = []
    for line in body.splitlines():
        row = ROW.match(line.strip())
        if not row:
            continue
        cells = [cell.strip() for cell in row.group(1).split("|")]
        if len(cells) < 2:
            continue
        document, updated = cells[0], cells[1].lower()
        if document.lower() in {"document", "documento"} or set(document) <= {"-"}:
            continue
        path = document.strip("`").strip()
        rows.append((path, updated))
    return rows


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: check-doc-traceability.py <base> [head]", file=sys.stderr)
        return 2
    head = sys.argv[2] if len(sys.argv) > 2 else "HEAD"
    base = diff_base(sys.argv[1], head)
    changed = [
        line
        for line in git(["diff", "--name-only", f"{base}...{head}"]).splitlines()
        if line
    ]
    changed_set = set(changed)
    errors: list[str] = []
    for path in changed:
        if not path.startswith("docs/openspec/changes/"):
            continue
        if "/archive/" in path or path.endswith("/archive"):
            continue
        if not path.endswith("/traceability.md"):
            continue
        try:
            text = open(path, encoding="utf-8").read()
        except OSError as exc:
            errors.append(f"{path}: unreadable ({exc})")
            continue
        rows = permanent_rows(text)
        if not rows:
            errors.append(
                f"{path}: Permanent documentation updated has no row marked yes"
            )
            continue
        for doc, updated in rows:
            if updated not in YES:
                continue
            if doc not in changed_set:
                errors.append(
                    f"{path}: claims `{doc}` updated, but that file is not in the diff"
                )
    if errors:
        print("✗ implementation does not validate the documents its traceability names")
        for error in errors:
            print(f"  {error}")
        print("  CONSTITUTION §8: update those permanent documents in this change.")
        return 1
    print("✓ claimed permanent documents are in the diff")
    return 0


if __name__ == "__main__":
    sys.exit(main())
