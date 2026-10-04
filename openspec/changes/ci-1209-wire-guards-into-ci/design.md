> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1209, Use Case CU76. Twelve top-level `scripts/test_*.py` guards had no wrapper in
`scripts/tests/`, the only directory CI and `preflight.sh` discover, so nothing enforced them.

## Goals / Non-Goals

**Goals:** discover every guard; prevent recurrence.
**Non-Goals:** changing what any guard asserts.

## Decisions

1. One thin wrapper per guard (importlib re-export), as in `test_repo_hygiene.py`. Rejected: a single auto-loader, because the issue asks for the established per-guard style and a missing file is exactly what the meta-guard detects.
2. No exemptions are used; the map exists so a future exception is a recorded decision.
3. External tools: `kustomize` skips via `unittest.SkipTest`; the Docker-dependent guards already skip.

## Riesgos / Trade-offs

- [A newly wired guard fails on `main`] → one did (CHANGELOG headings) and is fixed in this change.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| A guard without a wrapper fails the meta-guard | static | `scripts/tests/test_guard_wrappers.py` |
| Wrapped guards are collected | static | `python3 -m unittest discover -s scripts/tests` |
| A missing external tool skips | static | `scripts/test_staging_kustomize.py` |

- New unit tests (`src/test/java/.../unit/`): n/a
- New integration tests: n/a
- Coverage impact: none

## Regression Strategy

- Existing tests affected: none.
- Full suite command: `bash scripts/preflight.sh`.
- HTTP/Bruno API suite: unchanged.
- Legacy paths at risk: none.

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR, stacked on #1212
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit, `sdlc-process.yml` collects the new wrappers

## Rollback Strategy

- Revert the PR; test plumbing only.
