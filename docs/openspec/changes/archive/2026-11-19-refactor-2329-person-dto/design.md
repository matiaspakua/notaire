> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

The person entity applied a transport DTO. The only production caller was `User.setAttributes`.

## Goals / Non-Goals

**Goals:**

- `Person` no longer imports `DtoPerson`.
- Client fields are still skipped when the person is not a client.

**Non-Goals:**

- Removing `User.setAttributes`.

## Decisions

The mapper sits in the person persistence adapter. `User` repeats the field list so the business package does not depend on the adapter.

## Riesgos / Trade-offs

- [The field list exists in the mapper and in User] → the mapper test covers the client branch. The User slice can call the mapper later only if that dependency is acceptable, which it is not from `business`.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Client fields copy | unit | `PersonLegacyMapperTest` |
| Non-client skips nationality | unit | `PersonLegacyMapperTest` |
| No setAttributes on Person | unit | `PersonLegacyMapperTest` |

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
