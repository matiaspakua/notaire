# Workflow creates and updates answer 400 for missing or invalid data (#579 slice 3, #655)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #579 (slice 3, workflow group); #655 (empty bodies on the workflow endpoints) |
| Use Case | CU76 – Workflow por tipo de trámite; CU83 – Editor de workflows |
| Branch | `fix/579_constraint_errors_400_workflow` (stacked on `fix/579_constraint_errors_400_catalogs`, #1370) |
| Gate 1 status | draft |

## Objetivo

The three workflow controllers answered 409 when a create violated a NOT NULL constraint, 500 when a required id was missing (the null id reached `findById`) or an update broke a constraint, 500 for an unknown node `type`, and 200 for a node update that changed nothing. The Owner decided constraint errors are client errors and answer 400 (Run 7); #655 asks that empty bodies are rejected instead of failing with 500 or being accepted.

## What Changes

- `WorkflowDefinitionController`, `WorkflowNodeController`, `WorkflowTransitionController` use `ErrorResponses.createFailed`/`updateFailed` (from #1370): constraint errors answer 400; other failures keep 409/500.
- New `RequiredFields` helper (`require`, `requireEnum`, `parseEnum`): throws `BusinessValidationException` (400, standard `ErrorResponse`, `"field: reason"` message) for a missing id or an unknown enum value. It is used instead of bean validation because the controllers bind the shared `DtoWorkflow*` classes, which are also response schemas (#655 slice 1 lesson).
- Node `POST` requires `workflowDefinitionId`, `statusManagementId` and `type` (INITIAL, INTERMEDIATE, FINAL); node `PUT` requires at least one of `type`, `positionX`, `positionY` and rejects an unknown `type`; transition `POST` requires `workflowDefinitionId`, `originNodeId`, `destinationNodeId`; transition `PUT` requires `originNodeId` and `destinationNodeId`.
- OpenAPI: 400 documented on the three `PUT /{id}`; the 400 descriptions name the required fields. No schema change, no breaking change.
- Tests: `WorkflowConstraintErrorsIntegrationTest`, `ControllerExceptionMessageLeakTest` update, Bruno `workflow-definitions/08`, `workflow-nodes/09`–`10`, `workflow-transitions/13`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A create or update whose data violates a database constraint answers 400 | #579 (Owner decision Run 7) | Applied to workflow |
| A workflow node belongs to a workflow, has a management status and a type | V7 schema (NOT NULL) | Made explicit at the boundary |
| A transition links an origin and a destination node of a workflow | V7 schema (NOT NULL) | Made explicit at the boundary |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `api-error-contract`: Consistent HTTP status codes and error bodies for failed API requests.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | 3 workflow controllers, `RequiredFields`, OpenAPI, tests, Bruno |
| `frontend` | no | the workflow editor always sends the required fields (node drag sends positions) |
| Docs / scripts / CI | yes | CHANGELOG |

### Surface area

- Endpoints: `POST` and `PUT /{id}` of `/api/v1/workflow-definition`, `/workflow-node`, `/workflow-transition` (409/500/200 to 400 for missing or invalid data; 400 documented on PUT; not breaking)
- Entities / Flyway: none
- UI: none

### Architecture review

Same `ErrorResponses` helper as slices 1 and 2. `RequiredFields` keeps the shared DTO schemas unchanged. Checks run before the `try` blocks so the 400 is not turned into a 409 by the controller's catch-all. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
