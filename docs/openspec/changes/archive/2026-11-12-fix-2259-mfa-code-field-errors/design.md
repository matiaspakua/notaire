> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The MFA screens (merged in #2262) build on FormField and Input, so error wiring comes from FormField when an error string is passed. The challenge form used sonner for every failure.

## Goals / Non-Goals

**Goals:** every error about the second-factor code is announced and tied to its input.
**Non-Goals:** new error texts or contracts.

## Decisions

1. Reuse FormField's error for the login step (same pattern as the rest of the app); keep the existing message element for the enrolment step and only add the aria wiring, so its testid and tests are unchanged.
2. Connection failures stay toasts: they are not field errors.

## Riesgos / Trade-offs

- [Users who relied on the toast see an inline message] → it appears next to the code field and is announced as an alert.

## Testing Strategy

`login-page-mfa.test.tsx` (3 tests converted, 1 clearing, 1 connection guard) and `mfa-enrollment-panel.test.tsx` (1 extended), red first.

## Regression Strategy

Full Vitest suite with coverage floors; TS-0046 against a real stack.

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
