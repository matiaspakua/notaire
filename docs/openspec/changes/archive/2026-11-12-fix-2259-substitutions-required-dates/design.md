> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The page builds the payload straight from state and relies on the server's 400. The shared FormField provides error/aria wiring (#2088); other forms (managements, payments) already validate this way.

## Goals / Non-Goals

**Goals:** an empty substitution form tells the user which fields are missing, next to the field.
**Non-Goals:** new business rules on the dates.

## Decisions

1. Validate only what the API enforces (the two dates), so the client never blocks something the server accepts.
2. Same message key as the other forms (`common.fieldRequired`).

## Riesgos / Trade-offs

- [A server rule on dates the client misses] → unchanged: the server error is still presented.

## Testing Strategy

`substitutions-form-validation.test.tsx` (3 tests, red first).

## Regression Strategy

Full Vitest suite with coverage floors; the existing substitutions filter tests.

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
