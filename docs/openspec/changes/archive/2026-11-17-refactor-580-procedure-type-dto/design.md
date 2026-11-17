> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Procedure.getDto` and `DeedManagement` called `ProcedureType.getDto`. The ops adapter called `setAttributes` and `getDto`.

## Goals / Non-Goals

**Goals:**

- The procedure-type entity imports no transport DTO.
- Nested procedure DTOs still carry the same type fields.

**Non-Goals:**

- Removing `getDto` from `Procedure` or `DeedManagement`.

## Decisions

The mapper sits next to `ProcedureTypeOpsAdapter`. The two domain methods copy the fields themselves so `business` does not depend on the adapter.

## Riesgos / Trade-offs

- [The field list is copied in three places] → the mapper test and the existing entity tests cover the flags.
- [Other entities still map themselves] → #580 stays open.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Fields copy, an invalid DTO is ignored, no getDto on the entity | unit | `ProcedureTypeLegacyMapperTest` |

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
