# Workflow editor: visual editing with explicit controls (#1576)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1576 |
| Use Case | CU70 – Administrar workflow (nodos y transiciones) |
| Branch | `feat/1576_workflow_visual_editing` |
| Gate 1 status | draft |

## Objetivo

Epic #1573. Editing was hidden behind gestures: double-clicking a node or an edge deleted it without confirmation, a node's type could not be changed and a transition had no description. New nodes added in edit mode were also invisible until reload.

## What Changes

- `WorkflowSelectionPanel`: side panel for the selected status (type select, outgoing/incoming transitions, 'Add transition' form, delete) or transition (from → to, description, delete). Read-only outside edit mode.
- Every delete goes through `ConfirmDialog`; deleting a status that still has transitions warns before the server's 409.
- `checkNewTransition`: self-loops and duplicates rejected with a message, both from the panel and from dragging between handles.
- Double-click no longer deletes (and no longer zooms); click selects, clicking the pane clears.
- `useUpdateWorkflowTransition` (PUT /workflow-transition/{id}, existing endpoint).
- Fix: the controlled canvas kept React Flow's measured size out of its state, so a node added in edit mode stayed hidden; the view now re-fits after adding a node or auto-organizing.
- `ConfirmDialog` confirm button gets `data-testid=confirm-dialog-confirm`.
- i18n `administracion.workflows.editor.panel.*`, new hint (es/en).
- Tests: Vitest `workflow-edit.test.ts` (5), `workflow-selection-panel.test.tsx` (10); Playwright TS-0149. CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Deleting a status or a transition always asks for confirmation | CU70 | New (UI) |
| A transition cannot be a self-loop or a duplicate | CU70 | New (UI guard) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `workflow-editor-editing`: Adding, connecting, editing and deleting workflow statuses and transitions through visible controls.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | panel, edit guard, editor page, hook, ConfirmDialog test id, i18n |
| `testing/e2e` | yes | TS-0149 |

### Surface area

- Endpoints / entities / Flyway / configuration: none (uses existing PUT /workflow-node/{id} and PUT /workflow-transition/{id})

### Architecture review

Frontend only; a presentational panel component driven by one selection state on the editor page.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
