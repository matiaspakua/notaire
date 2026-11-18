> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`RegistrationDraftOpsAdapter` called `RegistrationDraft.getDto` on every read and write result. No other production type called it.

## Goals / Non-Goals

**Goals:**

- The registration-draft entity imports no transport DTO.
- The adapter still returns the same `DtoRegistrationDraft` fields.

**Non-Goals:**

- Changing the registration-draft HTTP contract.
- Removing `getDto` from other entities.

## Decisions

The mapper sits next to `RegistrationDraftOpsAdapter`. A missing deed still leaves `idDeed` null.

## Riesgos / Trade-offs

- [The field list is copied once] → the mapper test covers it, including a null deed.
- [Other entities still map themselves] → #580 stays open.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Fields copy onto the DTO | unit | `RegistrationDraftLegacyMapperTest` |
| A missing deed leaves the deed id empty | unit | `RegistrationDraftLegacyMapperTest` |
| The entity has no getDto | unit | `RegistrationDraftLegacyMapperTest` |
| The ratchet shrinks | unit | `EntityDtoMappingRatchetTest` |

TDD: the test did not compile until the mapper existed.

## Regression Strategy

- Mapper and ratchet tests.
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
