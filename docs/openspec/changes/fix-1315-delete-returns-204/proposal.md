# DELETE endpoints return the 204 No Content the contract documents (slice of #1315)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1315 |
| Use Case | CU15 – Procesar pago; CU13 – Gestionar historial de gestion; CU21 – Modificar Usuario (and the other CRUD use cases with a DELETE) |
| Branch | `fix/1315_delete_returns_204` |
| Gate 1 status | draft |

## Objetivo

The OpenAPI contract documents `204 No Content` for `DELETE /pagos/{id}`, `DELETE /historial/{id}` and 15 other DELETE endpoints, but they answered `200 OK` with an empty body. `DELETE /roles/usuarios/{idUser}` had the opposite mismatch (documented 200, returned 204). The Owner chose to make the implementation match the spec (204) and to audit every DELETE endpoint.

## What Changes

- 17 controllers: the delete handler returns `ResponseEntity.noContent()` instead of `ResponseEntity.ok()` (`Copy`, `SubmittedDocument`, `FolioType`, `History`, `Item`, `Management`, `ManagementStatus`, `Payment`, `IdentificationType`, `Procedure`, `Property`, `Substitution`, `Testimony`, `TestimonyMovement`, `WorkflowDefinition`, `WorkflowNode`, `WorkflowTransition`).
- `RoleController` documents the 204 it always returned (`@ApiResponses` 204/404); `openapi.yaml` regenerated.
- New guard `DeleteStatusMatchesContractTest`: for every `@DeleteMapping`, the documented 2xx codes equal the codes the handler returns.
- Existing MockMvc/integration tests and 21 Bruno requests assert 204.
- `accepted-breaking-changes.txt`: the roles/usuarios 200 to 204 documentation change, justified; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A DELETE handler returns exactly the success status its contract documents | #1315, CONSTITUTION (OpenAPI coherence) | New guard |
| Successful deletes answer 204 No Content | #1315 (Owner decision, Run 7) | Applied to 17 handlers |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `api-delete-status`: DELETE endpoints answer the success status documented in the OpenAPI contract.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | 18 controllers, OpenAPI, tests, Bruno |
| `frontend` | no | `apiDelete` checks only `res.ok`; its unit test already covers 204 |
| Docs / scripts / CI | yes | CHANGELOG, accepted-breaking-changes |

### Surface area

- Endpoints: the 17 DELETE endpoints above answer 204 instead of 200 (documented as 204 already); `DELETE /roles/usuarios/{idUser}` documentation 200 to 204 (runtime unchanged)
- Entities / Flyway / Configuration: none

### Architecture review

No architecture change. The guard is a source-level test in the web adapter test package, like the existing controller source guards.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `backend-api/openapi/openapi.yaml` | regenerated (roles/usuarios) |
| `backend-api/openapi/accepted-breaking-changes.txt` | accepted entry |
