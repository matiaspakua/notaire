"""Checks over the change's own diff against the base revision.

- `protected`: owner-only paths. Agents may propose a change to the policy, the gates or the harness,
  but a change that touches them is merged by a person, never under the agents' merge rule.
- `size`: a review budget. A diff a reviewer cannot read in one sitting is split into chained
  changes, so review stays a real check instead of a skim.
- `scope`: the write set. The change record names the paths the change may touch; anything else in
  the diff is either a missed planning step or an unapproved expansion of scope.

All three read committed history only (`base...HEAD`), like `tdd` and `ratchet`.
"""
from __future__ import annotations

import re
from pathlib import PurePosixPath

from aisdlc import repo
from aisdlc.context import Context

DEFAULT_SIZE_LIMIT = 400
HEADING = re.compile(r"^(#{1,6})\s+(.+?)\s*#*\s*$")
BACKTICKS = re.compile(r"`([^`]+)`")
NOTHING = {"none", "n/a", "—", "-"}
COMMENT = re.compile(r"<!--.*?-->", re.DOTALL)


def protected(ctx: Context, base: str | None) -> int:
    patterns = ctx.profile.protected_paths
    if not patterns:
        print("no owner-only paths configured ([protected] paths)")
        return 0
    base = base or ctx.profile.base
    hits = [p for p in repo.changed_files(ctx.root, base) if repo.matches(p, patterns)]
    if hits:
        print(f"  FAIL  {len(hits)} owner-only path(s) changed since {base}:")
        for path in hits:
            print(f"        {path}")
        print(f"protected: FAILED — agents may propose this change; {ctx.profile.protected_owner} reviews and merges it")
        return 1
    print("protected: no owner-only path changed — PASSED")
    return 0


def size(ctx: Context, base: str | None, limit: int | None) -> int:
    limit = limit or ctx.profile.size_limit or DEFAULT_SIZE_LIMIT
    base = base or ctx.profile.base
    counted, excluded, binary = 0, 0, 0
    rows = []
    for added, deleted, path in repo.numstat(ctx.root, base):
        if added is None or deleted is None:
            binary += 1
        elif repo.matches(path, ctx.profile.size_exclude):
            excluded += added + deleted
        else:
            counted += added + deleted
            rows.append((added + deleted, path))
    detail = f"{counted} changed lines counted ({excluded} excluded, {binary} binary file(s) not counted)"
    if counted > limit:
        print(f"  FAIL  {detail}, over the {limit}-line review budget. Largest files:")
        for lines, path in sorted(rows, reverse=True)[:8]:
            print(f"        {lines:6d}  {path}")
        print("size: FAILED — split it into chained changes (one reviewable unit each), "
              "or record why it cannot be split")
        return 1
    print(f"size: {detail} of {limit} — PASSED")
    return 0


def scope(ctx: Context, base: str | None, strict: bool) -> int:
    base = base or ctx.profile.base
    changed = repo.changed_files(ctx.root, base)
    spec_dir = ctx.profile.spec_dir.rstrip("/")
    records = _change_records(ctx, changed, spec_dir)
    if not records:
        verdict = "FAILED" if strict else "NOT EVALUABLE"
        print(f"scope: no change record in this diff declares a '{ctx.profile.scope_heading}' section — {verdict}")
        return 1 if strict else 0
    allowed: list[str] = []
    for record, patterns in records.items():
        print(f"  write set from {record}: {', '.join(patterns) or '(empty)'}")
        allowed += patterns
    outside = [p for p in changed
               if not _under(p, spec_dir) and not repo.matches(p, ctx.profile.scope_always)
               and not _in_write_set(p, allowed)]
    if outside:
        print(f"  FAIL  {len(outside)} changed path(s) outside the declared write set:")
        for path in outside:
            print(f"        {path}")
        print("scope: FAILED — update the change record (a planning gap) or take the path out of this change")
        return 1
    print(f"scope: {len(changed)} changed path(s), all inside the write set — PASSED")
    return 0


def _under(path: str, folder: str) -> bool:
    return path == folder or path.startswith(folder + "/")


def _change_records(ctx: Context, changed: list[str], spec_dir: str) -> dict[str, list[str]]:
    """Every change-record folder that this diff touches and that declares a write set."""
    folders: set[str] = set()
    for path in changed:
        if not _under(path, spec_dir):
            continue
        parent = PurePosixPath(path).parent
        while str(parent) != spec_dir and _under(str(parent), spec_dir):
            folders.add(str(parent))
            parent = parent.parent
    records: dict[str, list[str]] = {}
    for folder in sorted(folders):
        directory = ctx.path(folder)
        if not directory.is_dir():
            continue
        for document in sorted(directory.glob("*.md")):
            patterns = write_set(document.read_text(encoding="utf-8"), ctx.profile.scope_heading)
            if patterns is not None:
                records[f"{folder}/{document.name}"] = patterns
    return records


def write_set(text: str, heading: str) -> list[str] | None:
    """The patterns listed under the heading, or None when the document has no such heading.
    Each list item contributes its backticked spans, or its first word when it has none.
    HTML comments are template guidance, never part of the write set."""
    level, patterns = None, []
    for line in COMMENT.sub("", text).splitlines():
        match = HEADING.match(line.strip())
        if match:
            if level is not None and len(match.group(1)) <= level:
                break
            if level is None and match.group(2).strip().lower() == heading.lower():
                level = len(match.group(1))
            continue
        if level is None:
            continue
        stripped = line.strip()
        if not stripped.startswith(("- ", "* ")):
            continue
        item = stripped[2:].strip()
        spans = BACKTICKS.findall(item) or ([item.split()[0]] if item else [])
        patterns += [s.strip() for s in spans if s.strip().lower() not in NOTHING]
    return None if level is None else patterns


def _in_write_set(path: str, patterns: list[str]) -> bool:
    expanded = []
    for pattern in patterns:
        expanded.append(pattern)
        if pattern.endswith("/"):
            expanded.append(pattern + "**")
        elif not any(ch in pattern for ch in "*?["):
            expanded.append(pattern.rstrip("/") + "/**")
    return repo.matches(path, expanded)
