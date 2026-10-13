# Workflow graph: zoom, pan and fit-to-view (#1579)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1579 |
| Use Case | CU70/CU71 – Administrar workflow / trazabilidad |
| Branch | `feat/1579_workflow_zoom_fit` |
| Gate 1 status | draft |

## Objetivo

Epic #1573. The canvas controls had English accessible names, zoom was unbounded upward, the MiniMap was empty in view mode and in the read-only viewer (nodes never reported a size), and on a 390px phone fit-to-view could not show the whole workflow.

## What Changes

- `workflow-canvas.ts` (shared by editor and viewer): zoom bounded 0.1–2, fit padding, `ariaLabelConfig` with localized names (Acercar, Alejar, Ajustar a la vista, minimap), MiniMap color by node type, MiniMap hidden below md.
- Nodes carry `initialWidth/initialHeight` (fixed 200×64 box) so the MiniMap draws them without a measured size.
- Fit-to-view on load (#1575) and after auto-organize/add (#1576) kept; the Controls fit button uses the same padding.
- i18n `workflowViewer.canvas.*` (es/en).
- Tests: Vitest `workflow-canvas-chrome.test.tsx` (5); Playwright TS-0150 (zoom in then fit, MiniMap draws every box, fit on a phone, MiniMap hidden on a phone). CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Canvas controls have accessible names in the user's language | CU70 | New (UI) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `workflow-editor-viewport`: Zoom, pan, fit-to-view and the MiniMap of the workflow graph.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | canvas chrome, editor, viewer, i18n |
| `testing/e2e` | yes | TS-0150 |

### Surface area

- Endpoints / entities / Flyway / configuration: none

### Architecture review

Frontend only; one shared module for canvas options used by the editor and the read-only viewer.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
