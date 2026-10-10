# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1490 | open |
| Use Case | CU75 — Database Management and Migrations | exists |
| Specification | `docs/openspec/changes/refactor-1490-english-codebase/` | written |
| Branch | `cursor/english-codebase-01e0` | created from `origin/main` |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Users login path is English | `EnglishSurfaceRatchetTest` | passing |
| Spanish resource segment is not mapped | `EnglishSurfaceRatchetTest` | passing |
| People screen lives at the English route | dashboard folder `/dashboard/people` plus nav unit test | passing |
| Navigation hrefs do not use Spanish segments | `EnglishSurfaceRatchetTest` | passing |
| Identifications table is English | `EnglishSurfaceRatchetTest` | passing |
| Applied migrations are immutable | `git diff` against `origin/main` for V1–V44 | passing |
| Spanish catalog is still the Spanish locale | `frontend/src/tests/unit/i18n.test.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | yes | pending |
| `CHANGELOG.md` | yes | pending |
| `backend-api/openapi/openapi.yaml` | yes | pending |
| `backend-api/api-test/COVERAGE.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1490, this change folder |
| 2 | Failing tests written, test cases designed | yes | `EnglishSurfaceRatchetTest` red run, then green |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

Cloud agent branch policy requires the prefix `cursor/` and the suffix `-01e0`, so the branch is `cursor/english-codebase-01e0` rather than `refactor/1490_english_codebase`. No other Constitution step is skipped. Issue labeling `in-progress` failed with `Resource not accessible by integration`; the issue remains open as #1490.
