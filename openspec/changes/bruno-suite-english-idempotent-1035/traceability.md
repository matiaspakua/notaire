# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1035 | in-progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/bruno-suite-english-idempotent-1035/` | created |
| Branch | `test/1035_bruno_suite_english_idempotent` | created |
| Tasks | `tasks.md` | in progress |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | n/a — test suite only | pending |
| Smoke test | CI "API Tests (Bruno)" job | pending |

## Requirement coverage

`skip_specs: true` — no application behavior changes, so there are no
`#### Scenario:` acceptance criteria. The Issue's Acceptance Criteria are the
verification unit. Per-request Use Case / Requirement traceability lives in each
Bruno request's `Traceability:` description line and in
`docs/300-development/303-testing/CU-API-MATRIX.csv`.

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Suite English (folders, files, names, variables, env) | review + `grep` for Spanish identifiers | pending |
| Full suite passes on current DB | `bru run . -r --env Development` (run 1) | pending |
| Full suite passes again (idempotent) | `bru run . -r --env Development` (run 2) | pending |
| Every request cites CU + RF | `grep -L Traceability:` returns nothing | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/api-test/README.md` | pending | — |
| `backend-api/api-test/COVERAGE.md` | pending | — |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | pending | — |
| `docs/300-development/303-testing/README.md` | pending | — |
| `.github/workflows/playwright-e2e.yml`, `scripts/preflight.sh` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #1035, this plan, `.openspec.yaml` (`skip_specs: true`) |
| 2 | Failing tests written, test cases designed | yes | Baseline run 2026-09-24: 21/152 requests failing on a reused DB — the failing test is the second run |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
