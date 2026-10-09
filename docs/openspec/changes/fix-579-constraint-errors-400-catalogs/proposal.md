# Catalog creates and updates answer 400 for constraint errors (#579 slice 2, catalogs)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #579 |
| Use Case | CU26 – Tipos de trámite; CU27 – Tipos de documento; CU29 – Conceptos; CU30 – Estados de gestión; CU36 – Tipos de folio |
| Branch | `fix/579_constraint_errors_400_catalogs` |
| Gate 1 status | draft |

## Objetivo

When a create or update failed on a database constraint (e.g. `POST {}` violates NOT NULL on `name`), the catalog controllers answered 409 (create) or 500 (update), while `GlobalExceptionHandler` answers 400 for the same `DataIntegrityViolationException`. The Owner decided constraint errors are client errors and answer 400 (Run 7), one small PR per controller group.

## What Changes

- `ErrorResponses.createFailed` / `updateFailed` / `constraintViolation` / `isConstraintViolation`: a cause chain holding a `DataIntegrityViolationException`, a Hibernate `ConstraintViolationException` or an `SQLException` with SQLState class 23 answers 400 with the `GlobalExceptionHandler` message; other failures keep 409 (create) and 500 (update).
- Concept, DocumentType, FolioType, ManagementStatus and ProcedureType controllers use them on POST and PUT and document 400 on PUT.
- `openapi.yaml` regenerated (no breaking change); the seven accepted entries already on `main` removed.
- **Owner decision Oct 9:** `ErrorResponses.isUniqueViolation` (SQLState 23505, Hibernate `ConstraintKind.UNIQUE`, `DuplicateKeyException`); `constraintViolation` and `GlobalExceptionHandler` answer 409 with `The submitted data duplicates an existing record` for duplicates and 400 for the rest. `PUT /roles/{id}`, `POST /minutas-inscripcion` and `POST /cuadernos` document 409 (`PUT /roles/{id}` also its 400). Folder numbers (`procedure_folders`) are system-assigned and only collide under a race, so their callers are not documented with 409. `UniqueConstraintConflictIntegrationTest`; `ErrorResponsesTest` unique cases.
- Tests: `CatalogConstraintErrorsIntegrationTest`, `ErrorResponsesTest`, `ControllerExceptionMessageLeakTest` updates, Bruno `08-create-empty-body` in five folders; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A create or update whose data violates a database constraint answers 400 | #579 (Owner decision Run 7), GlobalExceptionHandler | Applied to catalogs |
| A unique-constraint violation (duplicate) answers 409; other constraint violations stay 400 | #579 (Owner decision Oct 9) | Changed, everywhere |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `api-error-contract`: Consistent HTTP status codes and error bodies for failed API requests.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `ErrorResponses`, 5 controllers, OpenAPI, tests, Bruno |
| `frontend` | no | catalog pages show the error for any non-2xx |
| Docs / scripts / CI | yes | CHANGELOG, accepted-breaking-changes |

### Surface area

- Endpoints: `POST` and `PUT /{id}` of `/api/v1/conceptos`, `/estado-gestion`, `/tipo-de-documento`, `/tipo-folio`, `/tipo-tramite` (constraint errors: 409/500 to 400; 400 documented on PUT; not breaking)
- Entities / Flyway: none
- UI: none

### Architecture review

The status decision lives in the shared `ErrorResponses` helper from slice 1, so the remaining controller groups switch with a one-line change each. DELETE handlers keep 409 for rows still referenced (state conflict, not a malformed request). No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `backend-api/openapi/accepted-breaking-changes.txt` | stale entries removed |
