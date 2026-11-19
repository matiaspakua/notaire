> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The notebook dialog disabled Create until a folio was checked, then toasted when that folio had no notary.

## Goals / Non-Goals

**Goals:** that failure is a field error on the folio checkboxes.
**Non-Goals:** enabling Create when nothing is selected.

## Decisions

1. Replace the toast. CheckboxField reads FormFieldContext so the alert is tied to the checkbox.
2. Clear the error when the selection changes.

## Riesgos / Trade-offs

- [Every checkbox in an invalid FormField is aria-invalid] → they share one alert, which is the group error.

## Testing Strategy

`notebooks-page-actions.test.tsx`: select a folio with no notary, expect aria-invalid, the alert, no toast, no create; toggling the checkbox clears the error. The test failed first on a missing aria-invalid.

## Regression Strategy

Other form tests stay. Vitest floors are not lowered.

## Playwright Strategy

No new spec. The dialog needs folio rows, and the shared E2E list can be empty. The unit test is the check.

## Deployment Strategy

Ships with the frontend image. No migration and no flag.

## Rollback Strategy

Revert the commit. The notebook API is unchanged.
