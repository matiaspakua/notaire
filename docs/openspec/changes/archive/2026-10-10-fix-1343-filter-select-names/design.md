# Design

## Context

Form selects are wrapped by `FormField`, which renders a `<label>`, so they are named; the five filters live in toolbars without one.

## Goals / Non-Goals

Goal: zero `button-name`/`select-name` violations on dashboard routes. Non-goals: replacing native selects with the shared `Select`, and the hardcoded Spanish status options on presupuestos (#1346).

## Decisions

`aria-label` instead of a visible label keeps the compact toolbars; the placeholder already shows the purpose visually. The static scan accepts a labelled `FormField` ancestor because that is how every form select is named today.

## Riesgos / Trade-offs

The scan is textual: a control named some other way would need `aria-label` or an exception. None exist today.

## Testing Strategy

`select-accessible-names.test.ts` (5 unnamed controls, keys missing, so red) and the `TS-0042` combobox tests written first.

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch build; axe on the five routes.

## Playwright Strategy

`TS-0042` (#1343 block): named comboboxes on gestiones, presupuestos and folios; on auditoria and estados-gestion when the control is rendered.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
