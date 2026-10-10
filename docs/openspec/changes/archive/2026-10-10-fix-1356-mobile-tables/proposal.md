# List pages render rows as cards on phones

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1356 |
| Use Case | RNF-05 – Aspecto visual; RNF-06; CU76 |
| Branch | `fix/1356_mobile_tables` |
| Gate 1 status | draft |

## Objetivo

At 390px every list page rendered the desktop table inside an `overflow-auto` wrapper: on `/dashboard/personas` only ID, Nombre and DNI/CUIL were visible, the edit and delete buttons were off-screen with no scroll affordance, and the search inputs were clipped ("Buscar por nomb"). The same happened on all 29 pages that use `DataTable`.

## What Changes

- `components/shared/DataTable.tsx`: below 768px (`useMediaQuery("(max-width: 767px)")`) rows render as a `<ul role="list">` of cards. The title is the first column that is not `id` (or one marked `mobile: "primary"`), the other columns are `<dt>`/`<dd>` pairs, and the `actions` column is a visible button row with 44px targets. Loading and empty states have card equivalents.
- `Column<T>` gains an optional `mobile?: "primary" | "actions" | "hidden"`; no page needs it today, so all 29 `DataTable` pages switch with no page edits.
- New `hooks/useMediaQuery.ts` (`useSyncExternalStore`, server snapshot `false`).
- Search inputs on personas, escrituras and presupuestos are `w-full` below `sm`.
- Vitest `data-table-mobile.test.tsx`; Playwright `TS-0109-mobile-lists`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| On phones every row's actions are reachable without horizontal scrolling | #1356, RNF-05, RNF-06 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `mobile-list-cards`: List pages are usable at phone widths.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | DataTable, useMediaQuery, 3 search bars |
| `testing` | yes | Playwright TS-0109 |
| `backend-api` | no |  |

### Surface area

- Routes: every /dashboard list page using DataTable (29) below 768px
- API: none

### Architecture review

No architecture change: one responsive branch inside the shared `DataTable`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
