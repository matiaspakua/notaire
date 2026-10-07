# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #595 | open → in progress |
| Use Case | CU13 – Gestionar historial de gestion | exists |
| Specification | `docs/openspec/changes/fix-595-management-status-history-lazy/` | Gate 1 draft |
| Branch | `fix/595_management_status_history_lazy` | created from updated `main` |
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
| Finding a status | `backend-api/src/test/java/com/licensis/notaire/integration/ManagementStatusHistoryFetchIntegrationTest.java` | passing |
| Listing statuses | `backend-api/src/test/java/com/licensis/notaire/integration/ManagementStatusHistoryFetchIntegrationTest.java` | passing |
| History read on demand | `backend-api/src/test/java/com/licensis/notaire/integration/ManagementStatusHistoryFetchIntegrationTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-595-management-status-history-lazy` |
| 2 | Failing tests written, test cases designed | yes | integration test written first and observed failing (collection loaded eagerly) |
| 3 | Suite green, coverage held, docs updated | partial | backend suite and Playwright green; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box.
