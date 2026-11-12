> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

FormField provides its state through FormFieldContext; Input and Select already read it with useFormFieldAria. SearchCombobox is used by PersonPicker, BudgetPicker, ManagementPicker and DeedPicker, so the fix reaches every form that uses them.

## Goals / Non-Goals

**Goals:** every error alert on the managements form is tied to an invalid control.
**Non-Goals:** changing the combobox behaviour or look.

## Decisions

1. Reuse `useFormFieldAria` (the repo's single mechanism) instead of new props, so all callers benefit with no changes.
2. The caller's own aria props are not part of this component's API today, so none are merged.

## Riesgos / Trade-offs

- [aria-describedby on a combobox changes announcements for four pickers] → it only adds the error text, matching Input and Select; verified in Chromium.

## Testing Strategy

`search-combobox-form-field.test.tsx` (4 tests, 2 red first) and Playwright TS-0159 (2 tests).

## Regression Strategy

Full Vitest suite with coverage floors (221 files, 1568 tests) and the existing picker tests.

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
