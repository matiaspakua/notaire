> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

The procedure entity built its transport DTO. Deed and testimony mapping called that method while copying a deed's procedures.

## Goals / Non-Goals

**Goals:**

- `Procedure` no longer builds `DtoProcedure`.
- Callers still copy the same fields, including the empty submitted-document list side effect.

**Non-Goals:**

- Moving `getDtoPerson` or `setDtoDocument`.
- Moving `DeedManagement.getDto`.

## Decisions

The mapper sits in the procedure persistence package. It uses getters so it does not need the entity's fields.

## Riesgos / Trade-offs

- [The procedure type is still required and throws if it is missing] → that is the previous behavior. The entity tests set a type.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Optional links empty | unit | `ProcedureLegacyMapperTest` |
| Deed, property, budget, management | unit | `ProcedureLegacyMapperTest` |
| Existing entity branches | unit | `ProcedureEntityTest` |

TDD: the mapper test did not compile until the class existed.

## Regression Strategy

- Mapper, entity, and ratchet tests.
- `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — no UI.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no
- Smoke: the mapper unit test

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
