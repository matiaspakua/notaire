# High-traffic business forms audit

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1951 |
| Use Case | RNF-09 – Uso de colores en la GUI |
| Branch | `feat/1951_business-forms-audit` |
| Gate 1 status | passed |

## Objetivo

Wave 5 of epic #1941. Cases, budgets, deeds, and people already compose
`FormContainer` / `FormField` / `FormActions`. Remaining gaps: required fields on
budgets/deeds/managements save without field-level errors; budgets/deeds lists lack
DataTable query `error` + retry; `loadError` i18n keys missing under those namespaces.

## What Changes

- Shared `requiredFieldErrors` helper (TDD).
- Budgets, deeds, managements create/edit: block save + `FormField` `error` for required empties.
- Budgets and deeds lists: DataTable `error` / `onRetry` (parity with people/managements).
- Add `loadError` (and field-required) messages under the four namespaces.
- FRONTEND-DESIGN-SYSTEM + CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Required fields fail visibly before submit | Form checklist / RNF-09 | Made explicit for budgets/deeds/managements |
| List query failures invite retry | #1947 | Extended to budgets/deeds |

## Capabilities

### New Capabilities

- `ui-business-forms`: required-field validation + list error parity for high-traffic forms.

### Modified Capabilities

- (none)

## Impact Analysis

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | helper, 3 pages, messages, tests, docs |
| `backend-api` | no | – |
