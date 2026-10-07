# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1279 | open → in progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related | #577 (epic, stays open), #1255 (DTO relocation) | referenced |
| Specification | `openspec/changes/refactor-1279-controller-records/` | Gate 1 draft |
| Branch | `refactor/1279_controller_signatures_records` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | in progress |
| Pull Request | — | passing |
| CI run | — | passing |
| Merge commit | — | passing |
| Release / tag | — | passing |
| Smoke test | — | passing |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| No entity in a handler signature | `ControllerSignatureArchitectureTest` | passing |
| Loose returns do not grow | `ControllerSignatureArchitectureTest` | passing |
| Folio and notebook expose a slim notary | `ControllerResponseRecordsIntegrationTest` | passing |
| Cost templates and procedures expose references by id and name | `ControllerResponseRecordsIntegrationTest` | passing |
| Available notaries are person records | `ControllerResponseRecordsIntegrationTest` | passing |
| Unused DTOs are gone | `DtoRemovalTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/openapi/openapi.yaml` | done | `b6a0f626` |
| `.claude/rules/refactoring.md` | done | — |
| `CHANGELOG.md` | done | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | yes | `d3f40954` |
| 3 | Suite green, coverage held, docs updated | passing | — |
| 4 | CI green, review approved, no conflicts | passing | — |
| 5 | Deployed, smoke test passed, Issue closed | passing | — |

## Exceptions

None.
