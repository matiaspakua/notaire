# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #596 | open → in progress |
| Use Case | CU13 – Gestionar historial de gestion | exists |
| Specification | `docs/openspec/changes/fix-596-history-pagination/` | Gate 1 draft |
| Branch | `fix/596_history_pagination` | created from updated `main` |
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
| Requested page size | `backend-api/src/test/java/com/licensis/notaire/integration/HistoryPaginationIntegrationTest.java, Bruno history/03` | passing |
| Default page | `backend-api/src/test/java/com/licensis/notaire/integration/HistoryPaginationIntegrationTest.java` | passing |
| Newest first | `backend-api/src/test/java/com/licensis/notaire/integration/HistoryPaginationIntegrationTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/openapi/accepted-breaking-changes.txt` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-596-history-pagination` |
| 2 | Failing tests written, test cases designed | yes | `HistoryPaginationIntegrationTest` observed failing before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend full suite, Bruno history and Playwright TS-0050 green locally; oasdiff passes with the accepted entry; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box.
