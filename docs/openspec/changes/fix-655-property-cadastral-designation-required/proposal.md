# A property is created and updated with its cadastral designation (slice of #655)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #655 |
| Use Case | CU69 – Gestión de Inmuebles; CU82 – Datos registrales del inmueble |
| Branch | `fix/655_property_cadastral_designation_required` |
| Gate 1 status | draft |

## Objetivo

`POST /api/v1/inmueble` stored a property without a cadastral designation (`{}` answered 201 on PostgreSQL), although the nomenclatura catastral is what identifies a property and the Inmuebles form already labels it required. The Owner decided it is required (#655, Run 7).

## What Changes

- `PropertyController.PropertyRequest.cadastralDesignation`: `@NotBlank`, documented `requiredMode = REQUIRED`. The record serves POST and PUT; PUT replaces every field, so it requires the designation too (otherwise it would erase it).
- Inmuebles page: Save stays disabled while the designation is blank.
- `openapi.yaml` regenerated; `accepted-breaking-changes.txt`: four entries (required + minLength on POST and PUT), justified.
- Tests: `PropertyRequestValidationIntegrationTest`, Bruno `properties/08`, Playwright TS-0019 CU69-GW04; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A property always carries a non-blank cadastral designation on create and update | CU69, #655 (Owner decision) | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `properties`: Registering and maintaining the properties (inmuebles) involved in a deed.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `PropertyController`, OpenAPI, tests, Bruno |
| `frontend` | yes | Inmuebles page |
| `testing` | yes | Playwright TS-0019 |
| Docs / scripts / CI | yes | CHANGELOG, accepted-breaking-changes |

### Surface area

- Endpoints: `POST /api/v1/inmueble`, `PUT /api/v1/inmueble/{id}` (cadastralDesignation required, minLength 1; breaking, accepted)
- Entities / Flyway: none (the entity already declares the column `optional = false`; no data migration)
- UI: Inmuebles dialog

### Architecture review

Bean validation on the existing request record, as for payments (#655 CU15). No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | BREAKING Changed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `backend-api/openapi/accepted-breaking-changes.txt` | four entries |
