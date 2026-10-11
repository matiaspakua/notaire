# Optional status filter on the paged deed list (#1340 slice 8a)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1340 |
| Use Case | CU06 – Firmar escritura; CU07 – Generar testimonio; CU82 – Minuta de inscripción |
| Branch | `feat/1340_deed_status_filter` |
| Gate 1 status | draft |

## Objetivo

The testimonios, minutas-inscripcion and folios screens offer only signed deeds, and they get them by loading `GET /deeds?size=1000` and filtering on the client. A searchable deed picker (slice 8b) cannot do that: a newest-20 page filtered on the client can be empty while signed deeds exist. The paged deed list gets an optional, non-breaking `status` filter so the server returns one page of deeds in one status.

## What Changes

- `GET /api/v1/deeds?status=<status>&page&size&sort`: optional `status`, one of `Sin Firmar`, `Firmada`, `Anulada`, `No Paso`, `Inscripta` (the stored `BusinessConstants` spellings), matched in any letter case; any other value answers 400 naming the accepted values. Without `status` the list is unchanged.
- `DeedRepository.findByStatus(String, Pageable)`, `DeedService.findAllPaged(String, Pageable)`.
- OpenAPI artifact regenerated (new optional query parameter with its enum, 400 with `ErrorResponse`); `frontend/src/types/api.generated.ts` regenerated.
- Bruno: `deeds/02-list-by-status.yml`, `deeds/03-list-invalid-status.yml`; `api-test/COVERAGE.md`.
- CHANGELOG entry. The deed pickers themselves are slice 8b (separate PR after this merges).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Deed statuses are Sin Firmar, Firmada, Anulada, No Paso, Inscripta | `BusinessConstants` | Made explicit in the API |
| Query filters on enumerated values accept any letter case and reject unknown values with 400 | #1346 (budget status) | Applied to deeds |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-list-pagination`: Lists and pickers backed by growing tables never load the whole table.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | DeedController, DeedService, DeedRepository, OpenAPI, Bruno |
| `frontend` | yes | generated API types only |
| `testing/e2e` | no | — |

### Surface area

- Endpoints: `GET /api/v1/deeds` gains optional `status` (non-breaking)
- Entities / Flyway / configuration: none

### Architecture review

No architecture change: a derived Spring Data query on the existing repository.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
| `backend-api/openapi/openapi.yaml` | status parameter |
| `backend-api/api-test/COVERAGE.md` | deeds row |
