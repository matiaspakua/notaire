"""Small process checks: always-loaded context budget, commit subjects, and tests-with-code."""
from __future__ import annotations

import re
from pathlib import Path

from aisdlc import repo
from aisdlc.context import Context
from aisdlc.context_budget import memory_pack_paths

BYTES_PER_TOKEN = 4


def budget(ctx: Context) -> int:
    limit = ctx.profile.context_max_tokens
    files = ctx.profile.context_files
    memory = ctx.profile.context_memory
    if limit is None or (not files and not memory):
        print("no context budget configured ([context] max_tokens, files|memory)")
        return 0

    measured: list[tuple[str, Path]] = []
    seen: set[Path] = set()
    missing: list[str] = []

    for rel in files:
        path = ctx.path(rel)
        if not path.is_file():
            missing.append(rel)
            continue
        resolved = path.resolve()
        if resolved in seen:
            continue
        seen.add(resolved)
        measured.append((rel, path))

    pack_paths, pack_errors = memory_pack_paths(ctx)
    for pack_id, path in pack_paths:
        resolved = path.resolve()
        if resolved in seen:
            continue
        seen.add(resolved)
        label = f"{path.relative_to(ctx.root) if path.is_relative_to(ctx.root) else path} (memory:{pack_id})"
        measured.append((label, path))

    total = 0
    for label, path in measured:
        size = path.stat().st_size
        total += size
        print(f"  {size:8d} bytes  {label}")

    tokens = total // BYTES_PER_TOKEN
    for rel in missing:
        print(f"  FAIL  always-loaded file {rel} is missing")
    for error in pack_errors:
        print(f"  FAIL  {error}")

    failed = bool(missing or pack_errors) or tokens > limit
    print(
        f"context: {tokens} tokens (est. bytes/{BYTES_PER_TOKEN}) of {limit} — "
        f"{'PASSED' if not failed else 'FAILED'}"
    )
    return 1 if failed else 0


def commits(ctx: Context, base: str | None) -> int:
    pattern = re.compile(ctx.profile.commit_pattern)
    subjects = repo.subjects(ctx.root, base or ctx.profile.base)
    bad = [s for s in subjects if not pattern.search(s)]
    for subject in bad:
        print(f"  FAIL  {subject}")
    print(f"commits: {len(subjects)} checked, {len(bad)} not matching {ctx.profile.commit_pattern} — "
          f"{'FAILED' if bad else 'PASSED'}")
    return 1 if bad else 0


def tdd(ctx: Context, base: str | None) -> int:
    production, tests = ctx.profile.tdd_production, ctx.profile.tdd_tests
    if not production:
        print("no production globs configured ([tdd] production)")
        return 0
    changed = repo.changed_files(ctx.root, base or ctx.profile.base)
    changed_tests = [p for p in changed if repo.matches(p, tests)]
    changed_code = [p for p in changed if repo.matches(p, production) and p not in changed_tests]
    if changed_code and not changed_tests:
        print("  FAIL  production code changed without any test change:")
        for path in changed_code:
            print(f"        {path}")
        print("tdd: FAILED — write the failing test first (it belongs in the same change)")
        return 1
    print(f"tdd: {len(changed_code)} production and {len(changed_tests)} test file(s) changed — PASSED")
    return 0
