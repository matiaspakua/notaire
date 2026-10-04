# Surface a gestión's escrituras, testimonios, copias and pagos in one case summary (slice of #774)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #774 |
| Use Case | CU07 – Generar testimonio (#160); CU11 – Ingresar para inscripción (#164); CU12 – Retirar testimonio (#165); CU15 – Procesar pago (#168); CU70 – Gestión de Copias (#243) |
| Branch | `feat/774_gestion_workflow_links` |
| Gate 1 status | draft |

## Objetivo

The back half of the case workflow exists as separate modules (escrituras, testimonios and their movements, copias, pagos) but a gestión shows none of it together. Add a case summary read model and a dialog on the gestiones screen so staff see, per gestión, its escrituras, their testimonios with the state of the registry circuit, the copias issued and the payments, each linked through the gestión's trámites.

## What Changes

- `GET /api/v1/gestiones/{id}/resumen-caso` returns the gestión header and, for each escritura reached through its trámites, the testimonios with verified flag, flagged flag, the state of the latest movement (SIN_INGRESAR, INGRESADO, INSCRIPTO, RETIRADO) and the number of copias.
- `ManagementCaseSummaryService` builds it; `DtoManagementCaseSummary`, `DtoCaseDeed` and `DtoCaseTestimony` live in `notaire-shared`.
- The gestiones screen gets a `Resumen del caso` action opening a dialog with Escrituras, Testimonios, Copias and Pagos; pagos come from the existing `resumen-financiero` endpoint.
- #774 stays open: creating each linked record directly from the gestión (rather than from its own module) and the full first-case journey spec are listed on the issue as the remaining work.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A gestión's escrituras are the distinct deeds of its trámites; testimonios and copias hang from those deeds | Domain model (Procedure.fkIdDeed, Testimony.fkIdDeed, Copy.fkIdTestimony) | Made explicit |
| The state of a testimony is the state of its latest movement, SIN_INGRESAR when it has none | CU11, CU12, CU44 | Made explicit |

## Capabilities

### New Capabilities

- `management-case-summary`: Show what a gestión has produced in the post-signing workflow.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | service and endpoint |
| `frontend` | yes | hook, dialog, i18n |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | notaire-shared DTOs, OpenAPI artifact, Bruno test, CU-API matrix, E2E spec |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Read model over existing aggregates; no schema change and no ADR. Follows the ManagementResumenFinancieroService pattern.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | row for the new endpoint |
