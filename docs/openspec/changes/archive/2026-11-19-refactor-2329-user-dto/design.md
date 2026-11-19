> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

The user entity applied a transport DTO. The live catalog adapter sets the password and role on the entity directly and does not call `setAttributes`.

## Goals / Non-Goals

**Goals:**

- `User` imports no transport DTO.
- The person copy goes through `PersonLegacyMapper` instead of a second field list.

**Non-Goals:**

- Changing the catalog adapter.

## Decisions

The mapper lives next to user persistence and calls `PersonLegacyMapper.apply`.

## Riesgos / Trade-offs

- [No production caller exercises the mapper] → the unit test is the behavior lock. Deleting the method without the mapper would drop the copy.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Account and person copy | unit | `UserLegacyMapperTest` |
| Null version is kept | unit | `UserLegacyMapperTest` |
| No setAttributes | unit | `UserLegacyMapperTest` |

TDD: the test did not compile until the mapper existed.

## Regression Strategy

- Mapper and ratchet tests.
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
