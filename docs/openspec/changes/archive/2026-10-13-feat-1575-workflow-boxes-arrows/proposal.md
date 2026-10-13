# Workflow editor: clear status boxes and directional arrows (#1575)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1575 |
| Use Case | CU70/CU71 – Administrar workflow (nodos y transiciones) |
| Branch | `feat/1575_workflow_boxes_arrows` |
| Gate 1 status | draft |

## Objetivo

Epic #1573: define the case workflow with boxes. Boxes were plain pills sized by their text and transitions thin lines without arrowheads, so direction and type were hard to read. Owner decisions 2026-10-12: status boxes only, left-to-right layout.

## What Changes

- `WorkflowNodeBox` (shared by editor and viewer): 200×64 box, wrapped name (2 lines, full name in title), text type badge, pill for start and double border for final, selection highlight, accessible name 'Estado X, tipo'.
- `workflowEdge`: smooth-step edge, closed arrowhead, label on a solid background truncated to 22 characters (full text kept in data).
- Layout: column gap 150 so labels fit between boxes; the editor canvas mounts once nodes and transitions are loaded so fit-to-view covers the whole graph.
- i18n `workflowViewer.types.*`, `workflowViewer.nodeAria` (es/en).
- Tests: Vitest `workflow-node-box.test.tsx` (6), viewer tests updated to read colors from node data; Playwright TS-0148 (badges and arrows, fit on load, selection). CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A workflow box is a management status; its type is readable as text | CU70 | New (UI) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `workflow-editor-boxes`: Drawing workflow statuses as boxes and transitions as arrows.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | node box, edge factory, editor, viewer, i18n |
| `testing/e2e` | yes | TS-0148 |

### Surface area

- Endpoints / entities / Flyway / configuration: none

### Architecture review

Frontend only; one node component and one edge factory shared by the editor and the viewer.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
