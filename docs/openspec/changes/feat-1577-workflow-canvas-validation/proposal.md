# Workflow editor: validation on the canvas, activation blocked while invalid (#1577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1577 |
| Use Case | CU70 – Administrar workflow (nodos y transiciones) |
| Branch | `feat/1577_workflow_canvas_validation` |
| Gate 1 status | draft |

## Objetivo

Epic #1573. 'Validar Workflow' listed English strings above the canvas and nothing showed which status was wrong. Owner decision 2026-10-12: an invalid workflow blocks saving and shows errors on the canvas.

## What Changes

- `validateWorkflow` (client): same rules and order as WorkflowValidationService (exactly one start, at least one final, all reachable from the start), each problem naming its nodes.
- Editor: live validation after every change; problem statuses get a dashed red outline, a '⚠' text mark and the problem in their accessible name; a localized problem list (or a 'valid' line) above the canvas.
- Saving blocked: the workflows list refuses to save a workflow as active while the backend validate says it is invalid (and refuses creating a new workflow as active, since it has no statuses yet); the dialog lists the problems in Spanish/English with a link to the editor.
- 'Validar Workflow' still calls the backend (source of truth) and shows its result.
- i18n `workflowViewer.problem.*`, `workflowViewer.problemList.*`, `administracion.workflows.activate*` (es/en).
- Tests: Vitest `workflow-validate.test.ts` (8), `workflow-node-invalid.test.tsx` (2); Playwright TS-0151. CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An active workflow must be valid (one start, a final, all reachable) | CU70 | New (UI guard; backend validate decides) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `workflow-editor-validation`: Showing workflow consistency problems on the canvas and gating activation on them.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | validation lib, node box, editor, workflows list, i18n |
| `testing/e2e` | yes | TS-0151 |

### Surface area

- Endpoints / entities / Flyway / configuration: none (uses existing POST /workflow-definition/{id}/validate)

### Architecture review

Frontend only. The client mirrors the backend rules for live feedback; the backend validate is called before activating.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
