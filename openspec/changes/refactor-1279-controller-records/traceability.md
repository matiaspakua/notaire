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
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| No entity in a handler signature | `ControllerSignatureArchitectureTest` | pending |
| Loose returns do not grow | `ControllerSignatureArchitectureTest` | pending |
| Folio and notebook expose a slim notary | `ControllerResponseRecordsIntegrationTest` | pending |
| Cost templates and procedures expose references by id and name | `ControllerResponseRecordsIntegrationTest` | pending |
| Available notaries are person records | `ControllerResponseRecordsIntegrationTest` | pending |
| Unused DTOs are gone | `DtoRemovalTest` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/openapi/openapi.yaml` | pending | — |
| `.claude/rules/refactoring.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
