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
| Tasks | `tasks.md` | in progress |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Identical dates return the later insert | `ManagementHistorialOrphanWriteIntegrationTest#shouldReturnLaterHistoryRowWhenDatesTie` | pending |
| Distinct dates still return the latest date | `ManagementHistorialOrphanWriteIntegrationTest#shouldReturnEstadoActualFromHistoryWhenRowsExist` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh fix-1198-estado-actual-tie-break` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
