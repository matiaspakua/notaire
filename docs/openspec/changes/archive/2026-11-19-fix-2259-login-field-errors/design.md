> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The login password step used one toast when either field was blank. The MFA code field already uses a field error.

## Goals / Non-Goals

**Goals:** each blank login field shows the required message on itself.
**Non-Goals:** changing lockout, connection, or rejected-login toasts.

## Decisions

1. Replace the blank-field toast. Keep the server-outcome toasts.
2. Clear only the field the user is typing in.

## Riesgos / Trade-offs

- [The same sentence appears on both fields when both are blank] → each control is tied to its own alert.

## Testing Strategy

`login-page.test.tsx` expects both inputs aria-invalid, no toast, no request, and the username error clears when a username is typed. The password error stays.

## Regression Strategy

Failed login, lockout, and connection tests still expect their toasts.

## Playwright Strategy

No new spec. The login page unit test submits the form without a session. Existing login Playwright stays on the happy path.

## Deployment Strategy

Ships with the frontend image. No migration and no flag.

## Rollback Strategy

Revert the commit. The login API is unchanged.
