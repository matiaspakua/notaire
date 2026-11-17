> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`History.getDto` called `ManagementStatus.getDto`. The entity imported the transport DTO. `setAttribute` throws when the DTO is invalid.

## Goals / Non-Goals

**Goals:**

- The status entity imports no transport DTO.
- An invalid DTO still throws `InvalidDtoException`.

**Non-Goals:**

- Removing `History.getDto`.

## Decisions

The mapper sits next to the management persistence adapter. `History` copies the four fields so `business` does not depend on the adapter.

## Riesgos / Trade-offs

- [The four fields are copied in two places] → the mapper test covers them.
- [Other entities still map themselves] → #580 stays open.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Fields copy, an invalid DTO is rejected, no getDto on the entity | unit | `ManagementStatusLegacyMapperTest` |

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
