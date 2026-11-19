> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

The adapter called `getDto` and `setAttributes`. `Testimony` did the same while copying its movement list.

## Goals / Non-Goals

**Goals:**

- The movement entity imports no transport DTO.
- Callers still copy the same fields.

**Non-Goals:**

- Removing `getDto` from `Testimony`.

## Decisions

The mapper sits next to the ops adapter. `Testimony` keeps its own copy.

## Riesgos / Trade-offs

- [The field list exists in the mapper and in Testimony] → the mapper test covers toDto and apply.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Fields copy, including the testimony id | unit | `TestimonyMovementLegacyMapperTest` |
| Apply copies id, card, notes, version, and testimony | unit | `TestimonyMovementLegacyMapperTest` |
| No getDto or setAttributes | unit | `TestimonyMovementLegacyMapperTest` |
| Existing entity assertions | unit | `EntitiesBasicTest` |

TDD: the test did not compile until the mapper existed.

## Regression Strategy

- Mapper, entities, and ratchet tests.
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
