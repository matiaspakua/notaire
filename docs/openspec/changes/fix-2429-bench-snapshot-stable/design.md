> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`generate-repolens-bench.py` builds a snapshot dict with `generated_at = datetime.now(timezone.utc).strftime(...)` on every invocation (line 200). The Next.js prebuild in `github-page` calls this script, so every Pages build overwrites the tracked `repolens-bench-snapshot.json` with a new timestamp, leaving the worktree dirty. The fix compares the would-be output (minus `generated_at`) with the existing file and preserves the old timestamp when the data is unchanged.

## Goals / Non-Goals

**Goals:**
- The tracked snapshot file stays byte-identical when only `generated_at` would differ.
- When bench data genuinely changes, a fresh `generated_at` is written.
- Existing `--check` mode and validation logic remain unchanged.

**Non-Goals:**
- Moving the snapshot to a git-ignored path.
- Making regeneration opt-in via a separate command.

## Decisions

1. **Compare data excluding `generated_at`** — The simplest fix: strip `generated_at` from both the new snapshot and the existing file, compare the JSON serializations, and reuse the existing timestamp if they match. This avoids introducing new config or file paths.
2. **Write byte-identical JSON** — When reusing the existing `generated_at`, serialize with the same `json.dumps(snap, indent=2) + "\n"` format so the file is byte-identical to what git tracks.
3. **No new CLI flags** — The behavior is transparent; no `--stable` flag is needed.

## Riesgos / Trade-offs

- [Risk] If the existing file has a different JSON key order, the comparison may falsely detect a change. → Mitigation: compare using `json.loads` (dict comparison) rather than raw string comparison, then serialize with the standard format.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Stable timestamp when data unchanged | unit | `workspace/tests/test_generate_repolens_bench.py` |
| New timestamp when data changes | unit | `workspace/tests/test_generate_repolens_bench.py` |

- New unit tests in `workspace/tests/test_generate_repolens_bench.py`:
  - `test_stable_timestamp_when_data_unchanged`: mock the existing snapshot with data matching the build output but a different `generated_at`; verify the written file keeps the old timestamp.
  - `test_new_timestamp_when_data_changes`: mock the existing snapshot with different data; verify a new `generated_at` is written.
- Coverage impact: Python tests, not covered by JaCoCo; Vitest floors unaffected.

## Regression Strategy

- Existing tests affected: `test_build_writes_validated_snapshot` in `test_generate_repolens_bench.py` — still valid, no changes needed.
- Full suite command: `python3 -m pytest workspace/tests/test_generate_repolens_bench.py -v`
- HTTP/Bruno API suite: n/a — no API changes.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface. This change affects a CI helper script and a JSON data file.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — merges to main trigger Pages rebuild automatically.
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): Run `python3 workspace/ci/generate-repolens-bench.py --check` and confirm git status is clean.

## Rollback Strategy

- Revert safe: yes — a standard `git revert` restores the previous behavior (always writing a new timestamp).
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: negligible — at worst the snapshot file gets a new timestamp on the next build, which is the pre-fix behavior.

## Migration Plan

No staged rollout needed — single PR merge.

## Open Questions

None.
