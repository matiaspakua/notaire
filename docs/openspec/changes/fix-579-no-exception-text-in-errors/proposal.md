# Controller catch-all errors no longer echo exception text (#579, slice 1)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #579 |
| Use Case | CU76 – Aseguramiento de Calidad e Infraestructura de Pruebas |
| Branch | `fix/579_no_exception_text_in_errors` |
| Gate 1 status | draft |

## Objetivo

Eleven controllers caught `Exception` and returned `e.getMessage()` as the 409/500 body. A request with a missing required field (for example `POST /api/v1/tipo-folio` with `{}`) answered with the raw PostgreSQL error: table and column names, the constraint, and `Failing row contains (...)` with the row's values. `PUT /api/v1/conceptos/{id}` leaked the JPA entity class name. That is information disclosure (CWE-209) and the body was not the standard `ErrorResponse`, so the frontend could not show it either.

## What Changes

- New `adapter/in/web/support/ErrorResponses` (`conflict(e)`, `serverError(e)`): standard `ErrorResponse` body, generic message unless the exception is an application-authored `NotaireException`, cause logged on the server with the request path.
- The 23 `catch (Exception e)` sites in Concept, DocumentType, FolioType, ManagementStatus, Person, ProcedureType, Testimony, TestimonyMovement, WorkflowDefinition, WorkflowNode and WorkflowTransition controllers use it. Each site keeps its status code (409 or 500).
- A source guard test fails if any controller puts a caught `Exception`'s message in a response again.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A caught exception's text is logged, never returned to the client, unless it is an application-authored `NotaireException` | #579 | New |
| Controller error bodies use the standard `ErrorResponse` shape | #579 | Extended to the catch-all sites |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `api-error-responses`: API error responses carry a safe, standard body and never expose internal exception text.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | 11 controllers, new `support/ErrorResponses`, tests |
| `frontend` | no | already reads `message` from JSON error bodies |

### Surface area

- Endpoints: no new endpoints; status codes unchanged; the error body of the 23 sites changes from raw text (or `{"error": <raw>}`) to `ErrorResponse`
- `openapi.yaml`: unchanged (error bodies were not documented)
- Entities / Flyway / Configuration: none

### Architecture review

The helper sits in `adapter/in/web/support` next to `CreatedResponses` (ADR-026). It builds the same `ErrorResponse` that `GlobalExceptionHandler` returns, so clients see one error shape.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Security entry |
