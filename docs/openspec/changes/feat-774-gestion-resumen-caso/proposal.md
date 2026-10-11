# Surface a management's deeds, testimonies, copies and payments in one case summary (slice of #774)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #774 |
| Use Case | CU07 – Generar testimonio (#160); CU11 – Ingresar para inscripción (#164); CU12 – Retirar testimonio (#165); CU15 – Procesar pago (#168); CU70 – Gestión de Copias (#243) |
| Branch | `feat/774_gestion_workflow_links` |
| Gate 1 status | draft |

## Objetivo

The back half of the case workflow exists as separate modules (deeds, testimonies and their movements, copies, payments) but a management shows none of it together. Add a case summary read model and a dialog on the managements screen so staff see, per management, its deeds, their testimonies with the state of the registry circuit, the copies issued and the payments, each linked through the management's procedures.

## What Changes

- `GET /api/v1/managements/{id}/case-summary` returns the management header and, for each deed reached through its procedures, the testimonies with verified flag, flagged flag, the state of the latest movement (SIN_INGRESAR, INGRESADO, INSCRIPTO, RETIRADO) and the number of copies.
- `ManagementCaseSummaryService` builds it; `DtoManagementCaseSummary`, `DtoCaseDeed` and `DtoCaseTestimony` live in `notaire-shared`.
- The managements screen gets a `Resumen del caso` action opening a dialog with Escrituras, Testimonios, Copias and Pagos; payments come from the existing `financial-summary` endpoint.
- #774 stays open: creating each linked record directly from the management (rather than from its own module) and the full first-case journey spec are listed on the issue as the remaining work.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A management's deeds are the distinct deeds of its procedures; testimonies and copies hang from those deeds | Domain model (Procedure.fkIdDeed, Testimony.fkIdDeed, Copy.fkIdTestimony) | Made explicit |
| The state of a testimony is the state of its latest movement, SIN_INGRESAR when it has none | CU11, CU12, CU44 | Made explicit |

## Capabilities

### New Capabilities

- `management-case-summary`: Show what a management has produced in the post-signing workflow.

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

Read model over existing aggregates; no schema change and no ADR. Follows the ManagementFinancialSummaryService pattern.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | row for the new endpoint |
