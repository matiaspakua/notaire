> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

The ratchet lists only `DeedManagement#getDto`. The method is called from the entity test. Nested notary and procedure copies stay on the entity so the mapper does not re-enter a removed `Person.getDto`.

## Goals / Non-Goals

**Goals:**

- `toDto` returns the same DTO the entity method returned.
- The baseline no longer names a mapping method.

**Non-Goals:**

- Moving `getDtoNotary` or `getDtoProcedure`.

## Decisions

The mapper calls the entity methods that are not on the ratchet. The entity does not depend on the adapter.

## Riesgos / Trade-offs

- [A hidden caller still expects `getDto` on the entity] → the only Java caller was the entity test, which now calls the mapper.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Null status and notary | unit | `DeedManagementEntityTest` |
| Status and notary present | unit | same |
| No mapping method remains | unit | `EntityDtoMappingRatchetTest` |

TDD: the test failed to compile because `DeedManagementLegacyMapper` did not exist.

## Regression Strategy

- `mvn -pl backend-api -Dtest=DeedManagementEntityTest,EntityDtoMappingRatchetTest test`
- `bash workspace/sdlc/preflight.sh --full` before push.

## Playwright Strategy

n/a — no product UI.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
