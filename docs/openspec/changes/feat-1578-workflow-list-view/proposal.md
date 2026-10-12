# Workflow editor: list view, keyboard and screen-reader path (#1578)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1578 |
| Use Case | CU70 – Administrar workflow (nodos y transiciones) |
| Branch | `feat/1578_workflow_list_view` |
| Gate 1 status | draft |

## Objetivo

Epic #1573. The graph was mouse-only: no keyboard path to add, connect or delete, and no screen-reader alternative. Owner decision 2026-10-12: on mobile, a list view for editing plus a read-only graph (#1580 uses this view).

## What Changes

- `WorkflowListView`: a statuses table (type select, outgoing transitions, problem mark, delete) and a transitions table (from → to, editable description, save, delete), plus 'add status' and 'add transition' forms; native controls, every one with an accessible name; deletes confirmed; self-loops/duplicates rejected.
- Editor: 'Diagrama' / 'Vista de lista' tabs (ARIA tablist, arrow keys); both views use the same handlers and hooks.
- Canvas boxes: the accessible name now includes the outgoing transition count ('Estado X, Inicial, 1 transición saliente'); boxes are focusable (React Flow default).
- i18n `administracion.workflows.editor.list.*`, `workflowViewer.outgoing` (es/en, ICU plural).
- Tests: Vitest `workflow-list-view.test.tsx` (9) and node name (1); Playwright TS-0152 (keyboard-only build, accessible names, focusable boxes). CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The workflow can be defined without a pointer | CU70 | New (accessibility) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `workflow-editor-list-view`: A table-based, keyboard-operable view of the workflow with the same edit actions as the graph.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | list view, editor tabs, node box, i18n |
| `testing/e2e` | yes | TS-0152 |

### Surface area

- Endpoints / entities / Flyway / configuration: none

### Architecture review

Frontend only; a presentational list component sharing the editor's handlers.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
