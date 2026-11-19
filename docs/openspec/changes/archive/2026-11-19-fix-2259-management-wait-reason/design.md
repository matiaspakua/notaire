> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

FormField takes an error string and wires aria-invalid and aria-describedby into Input. The managements folder dialog still used toast.error for a blank hold reason.

## Goals / Non-Goals

**Goals:** a blank hold reason is a field error tied to the reason input.
**Non-Goals:** changing the API, or the disabled-until-valid pattern on other pages.

## Decisions

1. Replace the toast rather than add an inline message next to it.
2. A boolean plus the translated message at render time, cleared when the typed reason is non-blank or the dialog closes.

## Riesgos / Trade-offs

- [Users who looked for a toast now see the message on the field] → the message stays next to the control and is an alert.

## Testing Strategy

`managements-page-actions.test.tsx` asserts aria-invalid, the described text, no toast, no request, clear on typing, and no error after the dialog is closed and opened again.

## Regression Strategy

The other managements action tests stay as they are. Vitest floors are not lowered.

## Playwright Strategy

`TS-0159` still covers the create dialog at 1280 and 320. The hold dialog needs a folder row. The shared E2E database can be empty (#2353), so this check is the unit test rather than a new spec that skips when the list is empty.

## Deployment Strategy

Ships with the frontend image. No migration and no flag.

## Rollback Strategy

Revert the commit. The hold API is unchanged.
