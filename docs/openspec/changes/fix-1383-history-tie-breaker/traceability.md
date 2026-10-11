# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1383 | open → in progress |
| Use Case | CU13 – Ver bitácora de una gestión | exists |
| Specification | `docs/openspec/changes/fix-1383-history-tie-breaker/` | Gate 1 draft |
| Branch | `fix/1383_history_tie_breaker` | created from updated `main` |
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
| Bitácora lists same-day entries in insertion order | `ManagementBitacoraServiceTest`, `HistoryOrderTieBreakerIntegrationTest`, Bruno `history/05` | passing |
| History by management is sorted | `HistoryOrderTieBreakerIntegrationTest`, Bruno `history/05` | passing |
| Workflow trace picks the last same-day entry as current | `WorkflowTraceServiceTest` | passing |
| Bitácora e2e no longer flaky | TS-0011, TS-0028 `--repeat-each` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1383-history-tie-breaker` |
| 2 | Failing tests written, test cases designed | yes | 3 JUnit tests failing on the pre-change code (red commit) |
| 3 | Suite green, coverage held, docs updated | yes | `backend-api/verify.sh` green; Bruno and e2e (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Bruno and Playwright ran against the branch jar on :8081 and a production frontend build.
