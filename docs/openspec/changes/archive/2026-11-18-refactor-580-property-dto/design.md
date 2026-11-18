> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Procedure.getDto` and `DeedManagement` called `Property.getDto`. The catalog adapter already maps the JPA row to `domain.property.Property`.

## Goals / Non-Goals

**Goals:**

- The property entity imports no transport DTO.
- Nested procedure DTOs still carry the same property fields.

**Non-Goals:**

- Removing `getDto` from `Procedure` or `DeedManagement`.

## Decisions

The mapper sits next to `PropertyCatalogAdapter`. The two domain methods copy the fields so `business` does not depend on the adapter.

## Riesgos / Trade-offs

- [The eight fields are copied in three places] → the mapper test covers them.
- [Other entities still map themselves] → #580 stays open.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Fields copy and the entity has no getDto | unit | `PropertyLegacyMapperTest` |

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

One pull request.

## Open Questions

None.
