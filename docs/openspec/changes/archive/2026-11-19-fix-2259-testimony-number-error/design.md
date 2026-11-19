> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The register dialog already stores `numberRequired` on the field. The edit dialog used `toast.error` for the same check.

## Goals / Non-Goals

**Goals:** a bad edit number is a field error tied to the number input.
**Non-Goals:** changing which strings count as a valid number.

## Decisions

1. Replace the toast rather than add a second message.
2. Clear the error when the typed value matches `^[1-9]\d*$`, and when the dialog closes.

## Riesgos / Trade-offs

- [The message moves from a toast to the field] → it stays next to the control and is an alert.

## Testing Strategy

`testimonies-page-actions.test.tsx` covers blank, zero, negative, decimal, letters, and leading zeros, plus clearing when a valid number is typed.

## Regression Strategy

The other testimony action tests stay. Vitest floors are not lowered.

## Playwright Strategy

No new spec. The edit dialog needs an existing testimony row, and the shared E2E list can be empty. The unit test is the check.

## Deployment Strategy

Ships with the frontend image. No migration and no flag.

## Rollback Strategy

Revert the commit. The update API is unchanged.
