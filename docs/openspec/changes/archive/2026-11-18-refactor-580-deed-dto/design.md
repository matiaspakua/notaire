> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Testimony.getDto` called `Deed.getDto`. `Procedure.getDto` already copies two deed fields and does not call it. `Folio` documents that it must not call `fkIdDeed.getDto()`.

## Goals / Non-Goals

**Goals:**

- The deed entity imports no transport DTO.
- A testimony DTO still carries the same nested deed.

**Non-Goals:**

- Removing `getDto` from `Testimony`.
- Changing the deed HTTP contract.

## Decisions

The mapper sits next to the deed persistence adapter. Testimony keeps its own copy of the field list so `business` does not depend on the adapter. `DtoDeed.getFolios()` returns a new empty list, so adding folios there does not stick; the mapper keeps that behavior.

## Riesgos / Trade-offs

- [The field list exists in the mapper and in Testimony] → both follow the previous method. The mapper test covers the scalar fields and the null procedure list.
- [Other entities still map themselves] → #580 stays open.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Fields copy and a missing procedure list stays null | unit | `DeedLegacyMapperTest` |
| The entity has no getDto | unit | `DeedLegacyMapperTest` |
| Existing deed DTO branches | unit | `DeedEntityTest` |
| The ratchet shrinks | unit | `EntityDtoMappingRatchetTest` |

TDD: the mapper test did not compile until the class existed.

## Regression Strategy

- `DeedEntityTest` calls the mapper.
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
