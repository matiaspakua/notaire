# Every form dialog is named by its visible title; the close button is localized

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1344 |
| Use Case | RNF-06 – Diseño de ventanas; CU76 |
| Branch | `fix/1344_dialog_accessible_names` |
| Gate 1 status | draft |

## Objetivo

Radix `Dialog` names `role=dialog` from a `Dialog.Title`. Only 2 dialogs used one: the other 39 `DialogContent` blocks in `src/app` render their heading through `FormSection`/`FormHeader`, so screen readers announced an unnamed dialog. The close button's hidden text was the hardcoded Spanish "Cerrar", also in English.

## What Changes

- `theme/form-patterns.tsx`: `FormSection` and `FormHeader` take `dialogTitle`; the heading (same `h3`/`h1`) is then rendered as Radix `Dialog.Title` via `asChild`, so `aria-labelledby` points at visible text.
- The first heading of each of the 39 dialogs gets `dialogTitle` (one title per dialog).
- `components/ui/dialog.tsx`: the close button text is `common.close` ("Cerrar"/"Close"); Radix's default `aria-describedby` is dropped because no dialog renders a description (callers can still pass one).
- i18n `common.close` (es/en).
- Vitest `dialog.test.tsx` (name, single title, no Radix warning, localized close, static scan of every `DialogContent` in `src/app`); Playwright `TS-0106`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every dialog has an accessible name from its visible title | #1344, RNF-06, WCAG 4.1.2 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-accessibility`: Dashboard controls and dialogs expose accessible names in the active locale.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | dialog, form-patterns, 25 page files, messages |
| `testing` | yes | Playwright TS-0106 |
| `backend-api` | no |  |

### Surface area

- Routes: every dashboard dialog
- API: none

### Architecture review

No architecture change: a prop on the shared form headings and a localized string in the dialog primitive.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
