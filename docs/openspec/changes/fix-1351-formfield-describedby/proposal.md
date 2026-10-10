# FormField links its error and helper text to the control and announces errors

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1351 |
| Use Case | RNF-08 – Especificación de campos a completar; CU76 |
| Branch | `fix/1351_formfield_describedby` |
| Gate 1 status | draft |

## Objetivo

FormField (the mandated form primitive) rendered its error and helper text as plain divs inside the <label>: the control was not described by them (they leaked into its accessible name instead), aria-invalid/aria-required were left to each caller, the error was not announced, and it started with a ⚠️ emoji.

## What Changes

- `theme/form-field-context.tsx`: `FormFieldContext` + `useFormFieldAria`/`mergeFormFieldAria` — FormField provides the error/helper id and the invalid/required state; the caller's own aria values win and its own `aria-describedby` ids are kept.
- `FormField`: ids via `useId()`; the error is `<p role="alert">` with a lucide `AlertCircle` (aria-hidden) and no emoji; helper text `<p>` with an id; both rendered outside the `<label>` so they describe the control instead of joining its name. A single native input/textarea/select child is cloned with the aria props.
- `ui/input.tsx` and `ui/select.tsx` (`SelectTrigger`) read the context, so every FormField-wrapped Input/Select gets `aria-describedby`, `aria-invalid` and `aria-required` without per-page wiring.
- Vitest `form-field.test.tsx`; Playwright TS-0015 and TS-0095 assertions; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An invalid field is marked invalid and described by its error | #1351, WCAG 1.3.1/3.3.1/4.1.2 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-forms`: Form field accessibility wiring.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | form-patterns, form-field-context, Input, SelectTrigger |
| `testing` | yes | Playwright TS-0015, TS-0095 |

### Surface area

- Every dialog form that uses FormField (35 files)

### Architecture review

No architecture change; one React context in the theme layer.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
