# Searchable budget picker instead of the first 1000 budgets (slice 6 of #1340)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1340 |
| Use Case | CU02 – Iniciar gestión; CU15 – Procesar pago |
| Branch | `fix/1340_budget_picker` |
| Gate 1 status | draft |

## Objetivo

The budget dropdowns of the new-management and payment forms were Radix selects fed by `usePresupuestos()`, i.e. `GET /presupuestos?size=1000` (newest first). The dev DB has about 1300 budgets, so the oldest ones could not be chosen for a management or a payment, and every dialog rendered 1000 options.

## What Changes

- New `components/shared/SearchCombobox.tsx`: the ARIA 1.2 combobox shell extracted from `PersonPicker` (input `role=combobox`, listbox popup, keyboard, Escape that does not close the dialog, polite status line, optional clear button, reduced motion), generic over the option type; the caller supplies the options for the debounced query.
- `PersonPicker` is now a thin wrapper over `SearchCombobox` (same behaviour and tests).
- New `components/shared/BudgetPicker.tsx` and `hooks/useBudgetSearch.ts`: an empty query lists the newest 20 budgets (`GET /presupuestos?page=0&size=20&sort=idBudget,desc`); a number is looked up as a budget number (`GET /presupuestos/{id}`) and as a client document; any text searches clients (`GET /people/search`, the PersonPicker query rules) and lists the budgets of the first 5 (`GET /presupuestos/persona/{id}`), newest first; the selected budget is loaded by id. Options read `#<number> — <client>` with the amount as secondary text.
- gestiones (new management) and pagos (payment form) use the picker; the payment field label is translated (`pagos.fields.presupuesto`). `usePresupuestos()` (size=1000) is removed.
- i18n `budgetPicker.*` in es and en.
- Vitest `budget-picker.test.tsx`; Playwright `TS-0114`; TS-0014 and TS-0011 type the client into the picker; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every budget can be chosen in a form, however many budgets exist | #1340, CU02, CU15 | Made explicit |
| A budget is found by its number or by its client's name or document | #1340, CU15 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-list-pagination`: Lists and pickers backed by growing tables never load the whole table.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | SearchCombobox, PersonPicker, BudgetPicker, useBudgetSearch, gestiones, pagos, usePresupuestos |
| `testing` | yes | Playwright TS-0114, TS-0014, TS-0011 |
| `backend-api` | no | GET /presupuestos (paged), /presupuestos/{id}, /presupuestos/persona/{id} and /people/search already exist |

### Surface area

- Routes: /dashboard/gestiones, /dashboard/pagos
- API: GET /api/v1/presupuestos?page=0&size=20&sort=idBudget,desc, GET /api/v1/presupuestos/{id}, GET /api/v1/presupuestos/persona/{id}, GET /api/v1/people/search (unchanged contract)

### Architecture review

No architecture change. The combobox shell is shared by both pickers; a unit test fails if any page or component calls `usePresupuestos()` again.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
