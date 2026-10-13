# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1315 | open → in progress |
| Use Case | CU49 – Eliminar Plantilla Presupuesto | exists |
| Specification | `docs/openspec/changes/fix-1315-budget-template-delete-204/` | Gate 1 draft |
| Branch | `fix/1315_budget_template_delete_204` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | local, not pushed |
| Pull Request | — | pending (Owner approval) |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Delete answers 204 | `backend-api/src/test/java/com/licensis/notaire/integration/BudgetTemplateControllerIntegrationTest.java, Bruno budget-templates/05` | passing |
| Every DELETE documents 204 | `backend-api/src/test/java/com/licensis/notaire/adapter/in/web/DeleteStatusMatchesContractTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/openapi/accepted-breaking-changes.txt` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1315-budget-template-delete-204` |
| 2 | Failing tests written, test cases designed | yes | `DeleteStatusMatchesContractTest.deleteHandlersDocumentNoContent` and `BudgetTemplateControllerIntegrationTest.shouldPersistBudgetTemplateDeletion` observed failing before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend verify, Bruno and Playwright green; oasdiff passes with one accepted entry; stale-entry check OK; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` and the Testcontainers `pg-integration` tests need Docker, unavailable on the agent box.
