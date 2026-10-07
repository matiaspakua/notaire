# Submitted document requests are validated at the boundary (slice of #655)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #655 |
| Use Case | CU04 – Gestionar documentos presentados |
| Branch | `fix/655_submitted_document_request_validation` |
| Gate 1 status | draft |

## Objetivo

`SubmittedDocumentController` accepted anything: a `date` that did not parse was silently dropped (and the shared lenient `SimpleDateFormat` turned `2026-02-30` into March 2), an unknown `typeId` was ignored, and on update an unknown `procedureId` unlinked the document. `PUT` on a stored document failed with 500. Reject invalid input with 400/404 and keep the OpenAPI artifact describing exactly that, without tightening the request schema.

## What Changes

- Strict `DateTimeFormatter` (`uuuu-MM-dd`, `ResolverStyle.STRICT`) replaces the shared `SimpleDateFormat`; an invalid date throws `BusinessValidationException` (400).
- Unknown `typeId` / `procedureId` throw `ResourceNotFoundException` (404) before anything is written.
- `update` is `@Transactional`; dates are converted through epoch millis (no `java.sql.Date.toInstant`); the due date is computed in calendar days.
- `@Schema` descriptions on the request fields and new 400/404 `@ApiResponse`s; `openapi.yaml` regenerated.
- Bruno `submitted-documents/10`, `11`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| `date` must be a real `yyyy-MM-dd` day, otherwise 400 | #655 | New |
| Unknown `typeId` / `procedureId` answer 404 and change nothing | #655 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `submitted-document-validation`: Submitted document create/update requests are validated before anything is stored.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `SubmittedDocumentController`, `openapi.yaml`, Bruno |
| `frontend` | no | — |
| Docs / scripts / CI | yes | CHANGELOG |

### Surface area

- Endpoints: `POST /api/v1/documento-presentado`, `PUT /api/v1/documento-presentado/{id}` (new 400/404 responses, descriptions only in the schema)
- Entities: none; Flyway: none
- Configuration: none

### Architecture review

No architecture change. Validation stays in the web adapter and uses the existing `GlobalExceptionHandler` mappings (`BusinessValidationException` → 400, `ResourceNotFoundException` → 404); references are resolved before the controller's `try` block so the generic catch does not swallow them.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `backend-api/openapi/openapi.yaml` | descriptions and responses |
