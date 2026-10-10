# Copy, registration draft and document re-entry require their fields (#655)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #655 |
| Use Case | CU08 – Copias de testimonio; CU82 – Minuta de inscripción; CU43 – Reingreso de documentación |
| Branch | `fix/655_required_ids_copy_draft_reentry` |
| Gate 1 status | draft |

## Objetivo

The #655 empty-body probe (re-run on `main` b39fcdaf, throwaway DB) found three endpoints that answer 500 for `{}`: `POST /api/v1/copia` (NOT NULL `print_date`), `POST /api/v1/minutas-inscripcion` and `POST /api/v1/gestiones/{id}/reingreso-documentacion` (a null id reached `findById`). Missing required data is a client error and must answer 400 naming the field.

## What Changes

- `CopyController.CopyRequest`: `number` and `datePrinting` are `@NotNull` and documented as required (POST and PUT; PUT replaces both columns).
- `RegistrationDraftController.GenerateRequest.idDeed` is `@NotNull`, and `generate` validates its body (`@Valid`).
- `DtoReingresoDocumentacionRequest.idProcedure` and `idDocumentType` are `@NotNull` (request-only record), and `ManagementController.reenterDocumentation` validates its body.
- Copies dialog: the print date is labelled required, and Save stays disabled until the number and print date are filled.
- `openapi.yaml` regenerated; the 7 `request property became required` breaks are accepted under #655 with the justification (no request without them ever succeeded). The 7 stale entries already on `main` are removed.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A copy has a number and a print date | V1 schema (NOT NULL) | Made explicit at the boundary |
| A registration draft is generated for a deed | CU82 | Made explicit at the boundary |
| A re-entry names the procedure and the document type | CU43 | Made explicit at the boundary |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `api-request-dto-binding`: REST write endpoints accept only validated request DTOs.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | 3 controllers, 1 request DTO, OpenAPI, tests, Bruno |
| `frontend` | yes | copies dialog requires number and print date |
| `testing` | yes | Playwright `copias-required-fields.spec.ts` |
| Docs / scripts / CI | yes | CHANGELOG, accepted-breaking-changes |

### Surface area

- Endpoints: `POST /copia`, `PUT /copia/{id}`, `POST /minutas-inscripcion`, `POST /gestiones/{id}/reingreso-documentacion` (500 to 400 for missing fields; required documented)
- Entities / Flyway: none
- UI: copies dialog

### Architecture review

Bean validation on controller-local or request-only records, as #1312/#1333/#1334 did; no shared response schema changes. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `backend-api/openapi/accepted-breaking-changes.txt` | 7 justified entries; stale entries removed |
