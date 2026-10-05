# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1282 | open → in progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related | #577 (epic, stays open), #580, #1279 | referenced |
| Specification | `openspec/changes/refactor-1282-dead-entity-dto-mapping/` | Gate 1 draft |
| Branch | `refactor/1282_entity_dto_mapping_removal` | created from updated `main` |
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
| A new mapping method is rejected | `EntityDtoMappingRatchetTest` | pending |
| A removed method must leave the baseline | `EntityDtoMappingRatchetTest` | pending |
| Production code does not need the deleted methods | `mvn verify` + OpenAPI export diff | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
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
