# Workflow editor on a phone: list view for editing, read-only graph (#1580)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1580 |
| Use Case | CU70 – Administrar workflow (nodos y transiciones) |
| Branch | `feat/1580_workflow_mobile` |
| Gate 1 status | draft |

## Objetivo

Epic #1573. At 390px the header buttons overflowed the screen and dragging boxes on a phone was impractical. Owner decision 2026-10-12: on mobile, a list view for editing plus a read-only graph.

## What Changes

- Below md (767px) the editor opens in the list view (#1578); the user's tab choice is kept.
- On a phone the graph is read-only even in edit mode (no drag, no connect, read-only side panel, no Auto-organizar), with a note pointing to the list view; fit-to-view and pinch-zoom stay.
- Header actions wrap instead of overflowing: `AppHeader` row and actions use flex-wrap (all pages) and the editor's own action group wraps.
- List view type select gets a minimum width; wide tables scroll inside their container, never the page.
- `resolveView` / `graphEditable` helpers.
- i18n `administracion.workflows.editor.list.phoneReadOnly` (es/en).
- Tests: Vitest `workflow-mobile.test.tsx` (4); Playwright TS-0153 (390px: list default, no overflow incl. header buttons, add a transition, read-only graph); TS-0150 phone tests open the diagram tab first. CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| On a phone the workflow is edited in the list view; the diagram is read-only | CU70 | New (UI) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `workflow-editor-mobile`: The workflow editor's behaviour on small screens.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | editor page, AppHeader, list view, view helpers, i18n |
| `testing/e2e` | yes | TS-0153, TS-0150 |

### Surface area

- Endpoints / entities / Flyway / configuration: none

### Architecture review

Frontend only; a media query selects the default view and disables graph editing.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
