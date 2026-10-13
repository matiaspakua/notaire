# Workflow editor: no overlapping boxes, auto-layout with saved positions (#1574)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1574 |
| Use Case | CU70/CU71 – Administrar workflow (nodos y transiciones) |
| Branch | `feat/1574_workflow_layout` |
| Gate 1 status | draft |

## Objetivo

Owner: 'the workflow UI is not seen correctly, all the boxes are overlapping'. Root cause: seeded nodes have null positions (mapped to 0,0); the editor's useNodesState kept its first, empty value (blank on direct load, stacked when cached); new nodes were placed over existing ones.

## What Changes

- `src/lib/workflow-layout.ts`: `layoutWorkflow(nodes, transitions, {force})` (dagre, left-to-right, saved positions kept unless missing or overlapping) and `nextFreePosition`.
- Editor: nodes and edges derived from the query data with local drag/selection overrides (`applyNodeChanges`); fixed-size boxes (200×64) so the layout is honest.
- 'Auto-organizar' (edit mode) re-lays out every node and saves positions via `PUT /workflow-node/{id}` (position-only update, already supported).
- New nodes go to free space; `WorkflowViewer` uses the same layout. Dependency `@dagrejs/dagre`. i18n `administracion.workflows.editor.autoLayout*`.
- Tests: Vitest `workflow-layout.test.ts` (7), `workflow-editor-sync.test.tsx` (1); Playwright TS-0147. CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A workflow node shows where it was saved; without a usable position it is placed automatically | CU70 | New (UI) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `workflow-editor-layout`: Laying out workflow nodes so boxes never overlap.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | layout helper, editor, viewer, i18n, dependency |
| `testing/e2e` | yes | TS-0147 |

### Surface area

- Endpoints / entities / Flyway / configuration: none (reuses `PUT /workflow-node/{id}`)

### Architecture review

Frontend only; layout is a pure function shared by the editor and the viewer.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
