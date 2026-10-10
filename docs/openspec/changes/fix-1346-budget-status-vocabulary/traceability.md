# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1346 | open → in progress |
| Use Case | RF-02 – Preparar presupuestos; RF-06 – Modificar presupuestos; CU01; CU45; CU60 – Buscar Presupuesto | exists |
| Specification | `docs/openspec/changes/fix-1346-budget-status-vocabulary/` | Gate 1 draft |
| Branch | `fix/1346_budget_status_vocabulary` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Case-insensitive write | `backend-api/src/test/java/com/licensis/notaire/adapter/in/web/BudgetStatusVocabularyTest.java` | passing |
| Unknown status | `backend-api/api-test/budgets/10-create-invalid-status.yml` | passing |
| Migration | `backend-api/src/test/java/com/licensis/notaire/integration/BudgetStatusMigrationIntegrationTest.java` | passing |
| Filter and edit | `testing/e2e/tests/TS-0115-budget-status.spec.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/openapi/accepted-breaking-changes.d/1346-budget-status-vocabulary.txt` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1346-budget-status-vocabulary` |
| 2 | Failing tests written, test cases designed | yes | Backend tests (enum missing), `budget-status.test.ts` (module missing) and TS-0115 (2 failed) observed failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | Backend `mvn test`, `bash frontend/verify.sh` green; Playwright chromium suite against the branch build and backend |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
