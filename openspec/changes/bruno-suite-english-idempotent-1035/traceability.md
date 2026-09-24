# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1035 | in-progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/bruno-suite-english-idempotent-1035/` | created |
| Branch | `test/1035_bruno_suite_english_idempotent` | created |
| Tasks | `tasks.md` | implementation done; PR/merge pending |
| Commits | `b7ace0a` (fix #1036), `5e0f246` (suite), `4e7a07a` (env rename), `a45689a` docs (`Closes #1035`), `1054df0` md-lint, `cddba90` (fix #1038, V38 folder-number sequence), `f34d567` (E2E fix #1037) | done |
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
| Suite English (folders, files, names, variables, env) | review + `grep` for Spanish identifiers | pass |
| Full suite passes on current DB | `bru run . -r --env Development` (run 1) | pass — 164/164 requests, 291/291 tests |
| Full suite passes again (idempotent) | `bru run . -r --env Development` (run 2) | pass — 164/164 requests, 291/291 tests; row counts unchanged except the append-only `audit_records` |
| Every request cites CU + RF | `grep -L Traceability:` returns nothing | pass |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/api-test/README.md` | yes | docs commit |
| `backend-api/api-test/COVERAGE.md` | yes | docs commit |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | yes | docs commit |
| `docs/300-development/303-testing/README.md` | yes | docs commit |
| `.github/workflows/playwright-e2e.yml`, `scripts/preflight.sh` | yes | `4e7a07a` |
| `CHANGELOG.md` | yes | docs commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #1035, this plan, `.openspec.yaml` (`skip_specs: true`) |
| 2 | Failing tests written, test cases designed | yes | Baseline run 2026-09-24: 21/152 requests failing on a reused DB — the failing test is the second run |
| 3 | Suite green, coverage held, docs updated | yes | Bruno 164/164 twice; `scripts/preflight.sh` 13 passed, 0 failed (after `b11ae29`) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

- Scope grew by one `backend-api/src` fix: making the suite idempotent exposed
  #1036 (budget-template DELETE returned 200 without deleting). Fixed in
  `b7ace0a` with its own integration + unit tests
  (`BudgetTemplateControllerIntegrationTest#shouldPersistBudgetTemplateDeletion`,
  `RemainingControllersJpaTest`), because the suite cannot clean up without it.
- Running the full E2E suite for the pipeline gate exposed two more defects,
  fixed on this branch because the gate cannot pass without them:
  - #1038 — concurrent case creation computed folder numbers as `max+1` and hit
    `uq_carpeta_tramite_numero`. Fixed in `cddba90` with Flyway V38
    (`procedure_folder_number_seq`), covered by `ProcedureFolderServiceTest` and
    `ProcedureFolderNumberSequencePgIntegrationTest` (20 concurrent draws).
  - #1037 — Playwright workers collided on generated ids and TS-0071 picked the
    seed budget. Fixed in `f34d567` (per-worker id stride, client-name match).
- Full pipeline (`scripts/run_pipeline.sh`) run on 2026-09-24: backend verify,
  pg-integration, frontend typecheck/eslint/vitest/build, HTTP, Bruno and
  Playwright suites.
