> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Both pages call the shared api-client helpers directly; the tests mock those helpers and route by path.

## Goals / Non-Goals

**Goals:** fast tests for two pages' business rules.
**Non-Goals:** the other large pages.

## Decisions

1. **Mock the api-client helpers**, not fetch, so assertions read as endpoint and body.
2. **Prove the tests can fail** by breaking the version resend and the in-use guard once.

## Riesgos / Trade-offs

- [Brittle selectors] → tests use the pages' existing `data-testid` values.

## Testing Strategy

The new files plus the full coverage run (956 tests); the mutation check failed exactly the two intended tests.

## Regression Strategy

`npx vitest run --coverage` in `frontend`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
