# Design

## Context

`/dashboard/gestiones` read `GET /gestiones?size=1000` through `useGestiones()` and rendered every row; the dashboard counted that array and took its first element as the newest management. Slice 2 (#1392) added `useUrlPagination`, `useClampPage` and the `DataTable` `pagination` prop; this slice is stacked on #1394, which carries them.

## Goals / Non-Goals

- Goal: the managements list reads one server page, shows the total and reaches every management; the dashboard reads only what it shows.
- Non-goal: the gestión and presupuesto selects on other screens (later slices); any backend change.

## Decisions

1. **Reuse slice 2.** This slice only wires the existing hooks and footer to `/gestiones`.
2. **Sort `idManagement,desc`.** `GET /gestiones` has no default sort; newest first keeps a management just created on page 1.
3. **Client filter stays unpaged.** `GET /gestiones/cliente/{id}` returns one client's managements; the footer is hidden while it is active.
4. **Dashboard reads `size=1`.** The counter needs only `totalElements`. The hero keeps its previous target (the first management in backend order, `sort: null`): following the newest one left the tracker empty when that management has no workflow (TS-0035), which is a product decision outside this slice.

## Riesgos / Trade-offs

- A page past the end (stale link, rows deleted) is clamped to the last page by `useClampPage`.
- The hero's "latest" is still the first management in backend order (in practice the oldest); reported, not changed.

## Testing Strategy

Vitest `gestiones-pagination.test.tsx`: hook URL and total, page render with the footer and no size=1000, URL page/size and clamping.

## Regression Strategy

`bash frontend/verify.sh` (lint, types, all Vitest, build); Playwright chromium suite against the branch production build.

## Playwright Strategy

`TS-0112`: 20 rows newest first, total, last page reaches the oldest management, dashboard total without size=1000. `TS-0070` (tour) must pass; it failed on the #1394 build.

## Deployment Strategy

Frontend only; ships with the next frontend deploy.

## Rollback Strategy

Revert the PR; the backend contract is unchanged.
