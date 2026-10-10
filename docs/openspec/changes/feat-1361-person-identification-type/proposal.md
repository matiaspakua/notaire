# Identification type in the person form, DNI by default (#1361, slice 1)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1361 |
| Use Case | RF-37 – Alta de clientes; CU17 – Dar alta persona; CU41 – Modificar cliente |
| Branch | `feat/1361_person_identification_type` |
| Gate 1 status | draft |

## Objetivo

The person dialog has a single free "DNI" field, so a foreign client (passport) or a company (CUIT) cannot be recorded with the right type, and the list header "DNI / CUIL" does not say which one a number is. The API silently stores DNI when the client sends an identification type id that does not exist. Owner decision on #1361: the type is **required when creating a person** and the form **defaults to DNI**; existing persons are not backfilled, and the edit form asks for a type on save when one is missing.

## What Changes

- Backend `PersonController`: `POST /people` and `PUT /people/{id}` answer **400** (`ErrorResponse`, message names `identificationTypeId`) when `identificationTypeId` / `fkIdIdentificationType.idIdentificationType` is not in the catalog; before, create stored DNI and update kept the old type with 200. An omitted type on create still means DNI, now found by name (`DNI`) instead of by id 1.
- OpenAPI: `PersonRequest.identificationTypeId` and `fkIdIdentificationType` documented (regenerated artifact; descriptions only, no schema change); `api.generated.ts` regenerated.
- Frontend: `useTiposIdentificacion` (GET `/identification-types`), a required "Tipo de identificación" select before the number in the person dialog (DNI preselected on create; a stored person without a type must pick one before saving), the number field is labelled "Número de documento", the list shows "CUIT 30…" (type name before the number), `Person.identificationTypeId`, i18n es/en.
- `contracts/api-reachability-allowlist.yaml`: `GET /api/v1/identification-types` removed (the UI now calls it).
- Bruno `people/08-create-unknown-identification-type.yml`; `people/01-create.yml` asserts the stored type; `COVERAGE.md`.
- E2E: TS-0015 #1361 scenarios; specs that located the number by the label `/dni/i` now use `input-dni`.
- CHANGELOG.

Out of scope (later slice of #1361): the admin catalog page `/dashboard/administration/tipos-identificacion` and the remaining allowlist entries. `maxLength` from the type's `characters` metadata is not enforced: legacy numbers with dots and test data exceed it, and the backend has no such rule.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A new person is created with an identification type; DNI when the client sends none | Owner decision on #1361 | Made explicit |
| An identification type id that is not in the catalog is rejected with 400 | #1361, #579 (400 for invalid input) | Changed (was silent DNI / silent keep) |
| The person form requires a type and preselects DNI | Owner decision on #1361 | New |
| Existing persons without a type are not backfilled | Owner decision on #1361 | Made explicit |

## Capabilities

### New Capabilities

- `person-identification-type`: Persons carry an identification type chosen in the form, DNI by default.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | PersonController type resolution, OpenAPI descriptions, Bruno |
| `frontend` | yes | personas page, new hook, types, i18n, generated API types |
| `contracts` | yes | reachability allowlist entry removed |
| `testing/e2e` | yes | TS-0015 scenarios, number-field locators |

### Surface area

- Endpoints: `POST /api/v1/people`, `PUT /api/v1/people/{id}` (new 400 for an unknown type id; 400 was already documented). No schema change, no migration.

### Architecture review

No architecture change: validation stays in the web adapter, which already resolves the type.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Changed entry |
| `backend-api/openapi/openapi.yaml` | field descriptions |
| `backend-api/api-test/COVERAGE.md` | people row |
