# Identification type admin catalog (#1361, slice 2)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1361 |
| Use Case | RF-53 – Administrar tablas base; CU17 – Dar alta persona (identification types) |
| Branch | `feat/1361_identification_type_catalog` |
| Gate 1 status | draft |

## Objetivo

Slice 1 (#1484) made the person form pick an identification type, but the catalog (DNI, LE, LC, Pasaporte, CUIT) has no admin screen: `POST/PUT/DELETE /api/v1/identification-types` are #1250 bucket C (UI backlog) in the reachability allowlist. Building that screen exposed a data-loss bug: `IdentificationType.personList` cascaded every operation (`CascadeType.ALL`) and `DELETE /identification-types/{id}` had no in-use check, so deleting a type that people use answered 204 and deleted those people with it. The catalog also accepted a second "dni" next to "DNI", which makes the DNI default for new people (looked up by name since slice 1) ambiguous.

## What Changes

- Backend `IdentificationTypeController`: `DELETE` answers **409** (`ErrorResponse`, "Cannot delete: the identification type is in use by people") while any person uses the type; `POST` and `PUT` answer **409** ("An identification type with this name already exists") for a name another type already has, ignoring letter case and surrounding spaces; the stored name is trimmed. `IdentificationType.personList` no longer cascades. `PersonRepository.existsByFkIdIdentificationTypeIdIdentificationType`.
- OpenAPI: new 409 responses on PUT and DELETE, 400 on PUT (regenerated artifact; no schema change); `api.generated.ts` regenerated.
- Frontend: admin page `/dashboard/administration/identification-types` (list with name and number length, create, edit, delete with confirmation; 409 on save → "Ya existe un tipo de identificación con ese nombre"; 409 on delete → the in-use toast from #1345), card in the administration index, breadcrumb label, create/update/delete hooks in `useIdentificationTypes.ts`, i18n es/en. ADMIN only through the existing `/dashboard/administration` route guard and `ADMIN_WRITE_PATHS`.
- `contracts/api-reachability-allowlist.yaml`: `POST`, `PUT` and `DELETE /api/v1/identification-types` removed (the UI calls them). `GET /identification-types/{id}` stays bucket A.
- Bruno `identification-types/06-09` (duplicate name 409, person with the type, delete in use 409, delete that person) with per-run unique names; the old `06-delete`/`07-verify-delete` become `10`/`11`. `COVERAGE.md`.
- E2E: TS-0139 (index link, create/edit/delete, in-use delete keeps the person, duplicate name, non-admin redirect). `PersonPayload.identificationTypeId` in the e2e helpers.
- CHANGELOG; testing inventory regenerated.

Out of scope: `characters` is NOT NULL in PostgreSQL but not validated in the request (a POST without it fails with 500); the page always sends it. The person form still does not enforce `maxLength` from `characters` (slice 1 decision).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An identification type that people use cannot be deleted | RF-53, #1345 (in-use 409) | Changed (was 204 + cascade delete of the people) |
| Removing an identification type never removes people | data integrity | New |
| One identification type per name, ignoring letter case and spaces | #1361 (DNI default by name) | New |
| Only ADMIN changes the catalog | #559 `ADMIN_WRITE_PATHS` | Made explicit (UI) |

## Capabilities

### New Capabilities

- `identification-type-catalog`: ADMIN maintains the identification types the person form offers.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | IdentificationTypeController guards, entity cascade, PersonRepository, OpenAPI, Bruno |
| `frontend` | yes | admin page, hooks, admin index, i18n, generated API types |
| `contracts` | yes | 3 reachability allowlist entries removed |
| `testing/e2e` | yes | TS-0139, helper payload type |

### Surface area

- Endpoints: `POST /api/v1/identification-types`, `PUT /api/v1/identification-types/{id}`, `DELETE /api/v1/identification-types/{id}` (new 409 cases). No schema change, no migration.

### Architecture review

No architecture change: the guards stay in the web adapter next to the repository calls it already makes, as `FolioTypeController` does.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added + Fixed entries |
| `backend-api/openapi/openapi.yaml` | 409/400 responses |
| `backend-api/api-test/COVERAGE.md` | identification-types row |
| `docs/300-development/303-testing/INVENTORY.md` | regenerated (TS-0139) |
