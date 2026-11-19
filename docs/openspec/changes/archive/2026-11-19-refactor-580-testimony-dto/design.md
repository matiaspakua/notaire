> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

The adapter called `getDto` and `setAttributes`. Those methods also copied the deed and the movement list.

## Goals / Non-Goals

**Goals:**

- The testimony entity imports no transport DTO.
- Callers still copy the same fields.

**Non-Goals:**

- Removing `Procedure.getDto`.

## Decisions

The mapper sits next to the ops adapter and keeps the previous field list, including the nested procedure DTO.

## Riesgos / Trade-offs

- [Nested `Procedure.getDto` remains] → a later Procedure slice replaces that call.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Fields, deed id, and movement copy | unit | `TestimonyLegacyMapperTest` |
| Apply keeps a null id | unit | `TestimonyLegacyMapperTest` |
| No getDto or setAttributes | unit | `TestimonyLegacyMapperTest` |
| Null deed | unit | `TestimonyEntityTest` |
| Existing entity assertions | unit | `EntitiesBasicTest` |

TDD: the test did not compile until the mapper existed.

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
