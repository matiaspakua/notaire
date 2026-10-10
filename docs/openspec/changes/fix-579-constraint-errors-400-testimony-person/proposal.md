# Testimony and person creates and updates answer 400 for constraint errors (#579 slice 4)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #579 |
| Use Case | CU07 – Testimonio; CU08 – Movimientos de testimonio; CU06 – Personas |
| Branch | `fix/579_constraint_errors_400_testimony_person` (stacked on `fix/579_constraint_errors_400_catalogs`, #1370) |
| Gate 1 status | draft |

## Objetivo

The last controllers that still caught exceptions themselves on create and update (testimony, testimony movement, person) answered 409 or 500 when the data violated a database constraint (e.g. `POST /movimiento-testimonio {}` and the NOT NULL entry date). The Owner decided constraint errors are client errors and answer 400 (Run 7).

## What Changes

- `TestimonyController`, `TestimonyMovementController` and `PersonController` use `ErrorResponses.createFailed`/`updateFailed` (from #1370) on POST and PUT: constraint errors answer 400 with the `GlobalExceptionHandler` message; other failures keep 409 (creates, person update) or 500 (testimony and movement updates). `DuplicatePersonException` keeps its 409 with `existingPersonId`.
- OpenAPI: the three `PUT /{id}` document 400 (person PUT already answered 400 for bean-validation errors without documenting it). No breaking change.
- Tests: `TestimonyPersonConstraintErrorsTest`, `TestimonyPersonConstraintErrorsIntegrationTest`, Bruno `testimony-movements/09-create-empty-body`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A create or update whose data violates a database constraint answers 400 | #579 (Owner decision Run 7) | Applied to testimony and person |
| A duplicate person answers 409 with the existing person's id | CU06 | Unchanged |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `api-error-contract`: Consistent HTTP status codes and error bodies for failed API requests.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | 3 controllers, OpenAPI, tests, Bruno |
| `frontend` | no | the testimony and person screens show the error for any non-2xx |
| Docs / scripts / CI | yes | CHANGELOG |

### Surface area

- Endpoints: `POST` and `PUT /{id}` of `/api/v1/testimonio`, `/movimiento-testimonio`, `/people` (constraint errors: 409/500 to 400; 400 documented on PUT; not breaking)
- Entities / Flyway: none
- UI: none

### Architecture review

Same `ErrorResponses` helper as slices 1–3. After this slice no controller answers a constraint error on POST/PUT with 409 or 500 through `ErrorResponses.conflict`/`serverError`. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
