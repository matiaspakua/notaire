> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

FormField takes an error string and wires aria-invalid/aria-describedby into Input through FormFieldContext (#2088). The items page kept the older toast-only validation for the conditional reason field.

## Goals / Non-Goals

**Goals:** every required error in the items dialog is tied to its control.
**Non-Goals:** changing which types require a reason.

## Decisions

1. Replace the toast rather than add inline next to it: one message, same as the administration forms.
2. State is a boolean plus the translated message at render time, so no stale string survives a locale change.

## Riesgos / Trade-offs

- [Users who relied on the toast see an inline message instead] → the message is visible next to the field and announced as an alert.

## Testing Strategy

`items-page-actions.test.tsx`: one updated test (red first), two new.

## Regression Strategy

Full Vitest suite with coverage floors; the other items tests unchanged.

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
