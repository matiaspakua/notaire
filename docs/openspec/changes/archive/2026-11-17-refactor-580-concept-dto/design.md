> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`ConceptOpsAdapter` was the production caller of `Concept.getDto` and `setAttributes`. The entity imported the transport DTO.

## Goals / Non-Goals

**Goals:**

- The entity file imports no transport DTO.
- Create and update keep the same null-field rules.

**Non-Goals:**

- Changing the concept HTTP API.

## Decisions

The mapper sits next to `ConceptOpsAdapter`. `apply` still declares `InvalidDtoException` because that was the old signature, even though the body does not throw it.

## Riesgos / Trade-offs

- [Other entities still map themselves] → #580 stays open.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Flags and amounts copy, and the entity has no getDto | unit | `ConceptDtoMappingTest` |

TDD: the test did not compile until the mapper existed.

## Regression Strategy

- `EntitiesBasicTest` calls the mapper.
- Full suite: `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — no UI surface.

- Specs to add/update under `testing/e2e/tests/`: none
- Command: `preflight.sh --full` still runs the suite

## Deployment Strategy

- Flyway: no
- Configuration: none
- Smoke: the mapper unit test

## Rollback Strategy

- Revert safe: yes
- Database rollback: none

## Migration Plan

One pull request. Further entities are later slices.

## Open Questions

None.
