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


def tdd(ctx: Context, base: str | None, labels: str | None = None) -> int:
    production, tests = ctx.profile.tdd_production, ctx.profile.tdd_tests
    if not production:
        print("no production globs configured ([tdd] production)")
        return 0
    waiver = _waiver(ctx.profile.tdd_waive_labels, labels)
    if waiver:
        print(f"tdd: waived by the {waiver!r} label")
        return 0
    if ctx.profile.tdd_order:
        return _tdd_order(ctx, base or ctx.profile.base)
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


def _waiver(waive_labels: tuple[str, ...], labels: str | None) -> str | None:
    given = {label.strip() for label in (labels or "").split(",")}
    return next((label for label in waive_labels if label in given), None)


def _tdd_order(ctx: Context, base: str) -> int:
    """Tests first, commit by commit: a commit changing both counts as tests first."""
    production, tests = ctx.profile.tdd_production, ctx.profile.tdd_tests
    seen_test, first_bad, code_commits = False, None, 0
    for short, subject, paths in repo.commit_files(ctx.root, base):
        seen_test = seen_test or any(repo.matches(p, tests) for p in paths)
        if any(repo.matches(p, production) and not repo.matches(p, tests) for p in paths):
            code_commits += 1
            if not seen_test and first_bad is None:
                first_bad = f"{short} {subject}"
    if not code_commits:
        print("tdd: no production code changed — PASSED")
        return 0
    if not seen_test:
        print("tdd: FAILED — production code changed but no test did; write the failing test first")
        return 1
    if first_bad:
        print(f"  FAIL  production code changed before any test: {first_bad}")
        print("tdd: FAILED — commit the failing test first (red), then the implementation (green)")
        return 1
    print(f"tdd: tests come before production code in {code_commits} commit(s) — PASSED")
    return 0
