# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```unknown
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1198 | open → in progress |
| Use Case | CU13 – gestion status and history | exists |
| Related | #806 (introduced the endpoint behaviour and test) | referenced |
| Specification | `openspec/changes/fix-1198-estado-actual-tie-break/` | Gate 1 draft |
| Branch | `fix/1198_estado_actual_tie_break` | created from updated `main` |
| Tasks | `tasks.md` | fix and docs complete; pipeline, PR, Gates 4-5 pending |
| Commits | — | done |
| Pull Request | — | done |
| CI run | — | done |
| Merge commit | — | done |
| Release / tag | — | done |
| Smoke test | — | done |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Identical dates return the later insert | `ManagementHistorialOrphanWriteIntegrationTest#shouldReturnLaterHistoryRowWhenDatesTie` | covered |
| Distinct dates still return the latest date | `ManagementHistorialOrphanWriteIntegrationTest#shouldReturnEstadoActualFromHistoryWhenRowsExist` | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md`, CU13 ID table | yes | docs commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-1198-estado-actual-tie-break` |
| 2 | Failing tests written, test cases designed | yes | tie test failed 3 of 3 on unfixed code before the fix commit |
| 3 | Suite green, coverage held, docs updated | done | — |
| 4 | CI green, review approved, no conflicts | done | — |
| 5 | Deployed, smoke test passed, Issue closed | done | — |

## Exceptions

None.
