# Undo the #1495 rename regressions that keep main red: Spanish es.json values, Spanish Playwright locators, the management create wire name (#1502)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1502 |
| Use Case | RNF-04 – Internacionalización (es-AR default locale); RNF-06 – Usabilidad |
| Branch | `fix/1502_es_json_spanish` |
| Gate 1 status | draft |

## Objetivo

The English identifier rename (#1495) also replaced entity words inside Spanish UI copy, so Spanish users saw toasts and labels such as "Person actualizada", "Property creado", "Nuevo Notebook" and "Role". Code identifiers are English; es.json values stay Spanish. The 27 affected values are restored from the pre-rename catalog and a guard keeps them from coming back. The same rename rewrote the Spanish UI text that Playwright locators match (/fecha/ to /date/, /presupuesto/ to /budget/, nav-gestiones to nav-managements) and left the management create sending procedureTypeId where CompleteCaseRequest reads typeProcedureId, so main stayed red after #1504 (50 E2E failures locally); this change fixes those too, plus the one-line generated-types drift that fails main's TypeScript Check.

## What Changes

- `frontend/messages/es.json`: 27 values restored from `d4bd1693` (personas, gestiones.fields, inmuebles, protocolo.cuadernos, administracion.usuarios/conceptos/plantillas/roles, suplencias). Sentence-case changes from #1495 that stay Spanish ("Movimientos de testimonio", "Método de pago") are kept. `en.json` untouched.
- Vitest `es-catalog-spanish.test.ts`: fails when an es.json value contains an English entity name from the rename map ("Roles" excluded: it is also Spanish).
- Playwright `TS-0023`: the users table header is "Rol" again (the rename had changed the assertion to "Role").
- Playwright specs (20 files): Spanish locator text restored from the pre-#1495 specs where the UI renders Spanish; English kept where the test runs in EN or matches the English API (`search?number=`, the `cadastral` message).
- `useCreateCompleteManagement` and the E2E helper `createCompleteCaseManagement` map `procedureTypeId` to `typeProcedureId` (shared `toCompleteCaseRequest`); Vitest `useManagements.create-wire.test.tsx`.
- `frontend/src/types/api.generated.ts` regenerated from the merged `openapi.yaml` (budget status description).
- Bruno: five assertions (`procedures/04a`, `procedure-types/04a`, `testimonies/04a`, `testimonies/04c`, `workflow-nodes/10`) expect the English validation messages the backend has returned since #1495.
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The create request uses the backend wire names | ManagementController.CompleteCaseRequest | Made explicit |
| es.json values are Spanish; only identifiers and keys are English | CONSTITUTION (English code, localized copy); #1490 scope | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `frontend-i18n-page-coverage`: Spanish UI copy stays Spanish after code renames.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | es.json, guard test, create mapper, generated types |
| `testing/e2e` | yes | 20 specs, seed helper |
| `backend-api` | no | — |

### Surface area

- Endpoints: none
- Entities / Flyway / configuration: none

### Architecture review

No architecture change: message catalog values only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
