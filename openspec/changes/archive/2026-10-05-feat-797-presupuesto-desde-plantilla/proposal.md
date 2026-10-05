# Create a presupuesto from a trámite-type template in one step (CU01, CU39)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #797 |
| Use Case | CU01 – Preparar Presupuesto (#154); CU39 – Crear Plantilla Presupuesto; CU71 – Gestión de Items |
| Branch | `feat/797_budget_from_template` |
| Gate 1 status | draft |

## Objetivo

Templates per procedure type can be loaded into an existing budget (#834) and catalog items added (#843), but the create form has no procedure-type selector, so staff must create the budget, reopen it and load the template by hand. Let the create form take an optional procedure type and load its template into the new budget.

## What Changes

- `useCreatePresupuesto` accepts an optional `tipoTramiteId`; after creating the budget it loads that type's template through the existing `POST /presupuestos/{id}/items-desde-plantilla` and reports whether the items were loaded.
- The presupuesto create form gains an optional `Tipo de trámite` selector; the items persist as `Item` rows through the existing endpoint, and each line stays editable on the Items screen (CU71).
- A type without template still creates the budget; the user sees a warning that no items were loaded instead of a failure.
- The amount field is labelled as the property value, and the budget total keeps coming from the items through the CU47 summary, so the two are no longer confusable.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Creating a budget with a procedure type loads that type's template items into it; without a type no items are added | #797 acceptance criteria 1-3 | New |
| A missing template never undoes the budget creation | CU39 exception (no template) | Made explicit |

## Capabilities

### New Capabilities

- `presupuesto-from-template`: Create a presupuesto and pre-fill its items from a procedure-type template in one step.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | create hook, form selector, i18n |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | E2E spec, CU01 doc |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Frontend composition of two existing endpoints; no backend or schema change and no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU01 – Preparar Presupuesto.md` | implementation note |
| `CHANGELOG.md` | one entry |
