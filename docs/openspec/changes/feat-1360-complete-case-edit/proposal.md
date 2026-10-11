# Full management edit through complete-case (#1360)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1360 |
| Use Case | CU02 – Crear gestión; CU53 – Modificar gestión; CU22/CU59 – Suplencia; RF-22 |
| Branch | `feat/1360_complete_case_edit` |
| Gate 1 status | draft |

## Objetivo

On `/dashboard/managements`, "Editar" opened the create dialog with only the Número field and saved `PUT /managements/{id}` with `{ number }`. A user who picked the wrong notary, budget, procedure type or property had to delete the management (losing its history) and create it again. The backend already had `PUT /managements/{id}/complete-case` (allowlisted as #1250 bucket C), but no read returned the ids it needs: `DtoManagementSummary` has no budget, notary, procedure type or property.

## What Changes

- Backend `ManagementController`: new `GET /api/v1/managements/{id}/complete-case` returning `CompleteCaseResponse` (idManagement, number, heading, notes, budgetId, notaryId, statusManagementId, typeProcedureId, propertyId), read from the same first procedure the PUT updates; 404 for an unknown management. Additive, non-breaking.
- OpenAPI regenerated; `api.generated.ts` regenerated; `ManagementCompleteCase` type.
- Frontend `useManagements`: `useManagementCompleteCase(id)` and `useUpdateCompleteManagement()` (PUT `/managements/{id}/complete-case`, CompleteCaseRequest wire names shared with create through `toCompleteCaseRequest`); `useUpdateManagement()` removed.
- The create side of the mapper (`procedureTypeId` → `typeProcedureId`) landed with #1502; the edit reuses its `toCompleteCaseRequest`.
- Managements page: the edit dialog renders every field, prefilled from the GET; the status select is disabled with a hint (CU83: status changes go through "Cambiar estado"); header and notes are sent back unchanged; the substitution toast shows only when the save added a note (a new redirect), not for an old redirect note; edit button `btn-edit-management-{id}`; i18n `gestiones.estadoViaTransition` es/en.
- `contracts/api-reachability-allowlist.yaml`: `PUT /managements/{id}/complete-case` removed (the UI calls it); `PUT /managements/{id}` added as bucket D (no UI consumer left).
- Bruno `procedure-folders/05b-05d` (GET, PUT, read-back); teardown seq shifted; `COVERAGE.md`.
- E2E TS-0126; CHANGELOG.

Out of scope: clearing a property (the PUT keeps the stored property when `propertyId` is null), editing the header and notes in the dialog, removing `PUT /managements/{id}`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A management's budget, notary, procedure type, property and number can be edited without deleting it | #1360, CU53 | New (UI) |
| An edit keeps the status; status changes go through the workflow transition | CU83, #804 | Made explicit (UI) |
| Editing to a notary with an active substitution redirects to the substitute and records a note | CU22/CU59 | Made explicit (UI toast on edit) |

## Capabilities

### New Capabilities

- `management-full-edit`: A management is edited in full from the list, prefilled from its current case values.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | new read endpoint, OpenAPI, Bruno |
| `frontend` | yes | managements page, hooks, types, i18n, generated API types |
| `contracts` | yes | reachability allowlist |
| `testing/e2e` | yes | TS-0126 |

### Surface area

- Endpoints: new `GET /api/v1/managements/{id}/complete-case`; `PUT /api/v1/managements/{id}/complete-case` gains a UI consumer. No schema change to existing operations, no migration.

### Architecture review

No architecture change: the read lives next to the PUT in the web adapter and uses the same repositories.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
| `backend-api/openapi/openapi.yaml` | new operation and schema |
| `backend-api/api-test/COVERAGE.md` | procedure-folders row |
| `contracts/api-reachability-allowlist.yaml` | entries |
