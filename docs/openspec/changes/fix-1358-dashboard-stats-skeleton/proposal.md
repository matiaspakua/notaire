# Dashboard stat cards: skeleton while loading, exact formatted totals, no list downloads

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1358 |
| Use Case | RNF-03 – Tiempo de respuesta; RF-23 |
| Branch | `fix/1358_dashboard_stats_skeleton` |
| Gate 1 status | draft |

## Objetivo

The dashboard used to download three lists of 1000 rows to count them. #1397 already switched the three cards to size=1 page requests that read totalElements; what remained was that each card showed 0 while its count loaded (and after an error), the total was not formatted, and no end-to-end check kept the dashboard off size=1000 requests.

## What Changes

- `components/dashboard/StatValue.tsx`: a pulse skeleton with a translated screen-reader 'loading' text and `aria-busy` while the count loads, a dash on error, and the exact total formatted for the locale (es-AR / en-US).
- `app/dashboard/page.tsx` passes each size=1 page query to `StatValue` instead of `totalElements ?? 0`.
- Vitest `dashboard-stat-value.test.tsx`; Playwright TS-0118 (counts equal the API totals, size=1 requests, no size=1000, skeleton while a count is held back). CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The dashboard never shows a placeholder 0 as a count | #1358 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-dashboard`: Dashboard summary cards.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | StatValue, dashboard page |
| `testing` | yes | Playwright TS-0118 |

### Surface area

- /dashboard

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
