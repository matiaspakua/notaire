# One budget status vocabulary: the stored codes, validated, migrated and translated

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1346 |
| Use Case | RF-02 – Preparar presupuestos; RF-06 – Modificar presupuestos; CU01; CU45; CU60 – Buscar Presupuesto |
| Branch | `fix/1346_budget_status_vocabulary` |
| Gate 1 status | draft |

## Objetivo

`budgets.status` was free text. The UI wrote and listed BORRADOR/APROBADO/RECHAZADO/FACTURADO as Spanish literals, while the E2E fixtures wrote "Pendiente" and the API tests "Pending". On the dev DB 1434 of 1536 budgets were "Pendiente": the status filter could not select them, their edit dialog showed a blank status, and the raw values showed untranslated.

## What Changes

- Owner default (2026-10-10): the vocabulary is the stored codes `BORRADOR, PENDIENTE, APROBADO, RECHAZADO, FACTURADO` (what the UI writes and the backend repository tests use); there was no backend enum.
- Backend: new `business/BudgetStatus` enum; `BudgetController` stores the code for POST/PUT in any letter case and answers 400 naming the accepted values for anything else (`RequiredFields.parseEnum`); `GET /presupuestos/buscar?status=` normalises the same way; OpenAPI declares the enum (accepted breaking change `1346-budget-status-vocabulary.txt`) and the 400 responses.
- Flyway `V44__budget_status_vocabulary.sql`: maps Spanish and English spellings in any case to the code; unknown values are kept upper-cased and trimmed (no data invented). Idempotent.
- Frontend: `lib/budget-status.ts` (`BUDGET_STATUSES`, `DEFAULT_BUDGET_STATUS`, `normalizeBudgetStatus`, `budgetStatusLabelKey`); the presupuestos list column, filter and edit select are generated from it with labels `presupuestos.status.<CODE>` (es/en); a legacy value outside the vocabulary stays visible in the edit select (disabled) instead of a blank select; the unused `presupuestos.states` keys are removed.
- Fixtures: 15 Java integration-test payloads, the api-test budgets (canonical; the update sends `aprobado` to prove case-insensitivity) and the Playwright fixtures send codes; new api-test `budgets/10-create-invalid-status.yml` (400).
- Tests: `BudgetStatusVocabularyTest`, `BudgetStatusMigrationIntegrationTest` (pg-integration), Vitest `budget-status.test.ts`, Playwright `TS-0115`; TS-0113 expects the translated label; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A budget status is one of BORRADOR, PENDIENTE, APROBADO, RECHAZADO, FACTURADO | #1346, RF-02, RF-06 | New |
| The UI shows a translated status label, never a raw code | #1346, RNF i18n | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `budget-status`: The budget status vocabulary shared by the API, the database and the UI.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | BudgetStatus, BudgetController, V44, OpenAPI, api-test |
| `frontend` | yes | budget-status lib, presupuestos page, i18n |
| `testing` | yes | Playwright TS-0115, TS-0113 and fixtures |

### Surface area

- API: POST/PUT /api/v1/presupuestos, GET /api/v1/presupuestos/buscar (status enum, 400 on unknown)
- DB: budgets.status values (V44)
- Route: /dashboard/presupuestos

### Architecture review

The enum lives in `business`; parsing and the 400 stay in the web adapter (no business → adapter dependency).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `backend-api/openapi/openapi.yaml` | status enum and 400 responses |
| `backend-api/openapi/accepted-breaking-changes.d/1346-budget-status-vocabulary.txt` | Accepted break |
