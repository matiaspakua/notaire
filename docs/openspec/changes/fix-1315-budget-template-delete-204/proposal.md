# The budget template concept DELETE returns 204 No Content like every other DELETE (follow-up of #1315)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1315 |
| Use Case | CU49 – Eliminar Plantilla Presupuesto |
| Branch | `fix/1315_budget_template_delete_204` |
| Gate 1 status | draft |

## Objetivo

After #1332 every DELETE handler returned the status its contract documents, but `DELETE /api/v1/plantilla-presupuestos/tipo-tramite/{idProcedureType}/concepto/{idConcept}` still documented (springdoc default) and returned 200. The Owner rule is that DELETE returns 204 everywhere.

## What Changes

- `BudgetTemplateController.delete` returns `ResponseEntity.noContent()` and documents 204 and 404.
- `DeleteStatusMatchesContractTest` gains a second guard: every `@DeleteMapping` documents 204 as its only success status.
- `BudgetTemplateControllerIntegrationTest` and Bruno `budget-templates/05-delete` expect 204.
- `openapi.yaml` regenerated; `accepted-breaking-changes.txt`: one entry, justified; the seven entries of #1332, #1333 and #1334 already on `main` removed; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Successful deletes answer 204 No Content, and every DELETE documents only 204 | #1315 (Owner rule, Run 7) | Guard extended |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `api-delete-status`: DELETE endpoints answer the success status documented in the OpenAPI contract.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `BudgetTemplateController`, OpenAPI, tests, Bruno |
| `frontend` | no | `apiDelete` checks only `res.ok` |
| Docs / scripts / CI | yes | CHANGELOG, accepted-breaking-changes |

### Surface area

- Endpoints: `DELETE /api/v1/plantilla-presupuestos/tipo-tramite/{idProcedureType}/concepto/{idConcept}` (200 to 204; breaking, accepted)
- Entities / Flyway: none
- UI: none

### Architecture review

Same pattern as #1332. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `backend-api/openapi/accepted-breaking-changes.txt` | one entry, seven stale ones removed |
