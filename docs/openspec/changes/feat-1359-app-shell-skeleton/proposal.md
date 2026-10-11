# App-shell skeleton instead of a blank first paint (#1359)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1359 |
| Use Case | RNF-03 – Tiempo de respuesta; RNF-05 – Usabilidad |
| Branch | `feat/1359_app_shell_skeleton` |
| Gate 1 status | draft |

## Objetivo

`app/dashboard/layout.tsx` returned `null` until the client had mounted and read the stored session, so the server HTML of every dashboard URL was empty and each hard load or refresh painted a white page before the sidebar and content popped in. The layout now renders a static app-shell skeleton (sidebar frame, header row, content blocks) with the real shell's widths, so the first paint is the shell and nothing shifts when it mounts. A shared `Skeleton` block replaces the ad-hoc pulse blocks and only pulses when the user allows motion.

## What Changes

- `frontend/src/components/layout/AppShellSkeleton.tsx`: `role="status"`, `aria-busy="true"`, `aria-label` = `common.loading`; desktop sidebar block `w-72` (as `AppSidebar`), header row and content blocks with the layout's paddings.
- `frontend/src/app/dashboard/layout.tsx`: returns `<AppShellSkeleton />` instead of `null` while `!mounted || !isAuthenticated`; the redirect to `/login` is unchanged.
- `frontend/src/components/ui/skeleton.tsx`: shared block, `aria-hidden`, `data-slot="skeleton"`, `motion-safe:animate-pulse` (a static tint under `prefers-reduced-motion`).
- `DataTable` (table and mobile cards) and the dashboard workflow hero use `Skeleton`; the dashboard stat skeleton pulses only with motion allowed. A guard test keeps plain `animate-pulse` out of components.
- Tests: Vitest `app-shell-skeleton.test.tsx` (+ `data-table`, `dashboard-stat-value` selectors); Playwright `TS-0125`.
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A dashboard hard load never paints a blank page | RNF-05, #1359 | New |
| Loading placeholders do not pulse under reduced motion | Motion design note (#1336), WCAG 2.3.3 | Made explicit |
| Unauthenticated users are redirected to /login | Existing layout rule | Unchanged |

## Capabilities

### New Capabilities

- `ui-app-shell-skeleton`: The dashboard paints a static app shell before it mounts, and loading placeholders respect reduced motion.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | layout, AppShellSkeleton, ui/skeleton, DataTable, WorkflowHero, StatValue |
| `testing/e2e` | yes | TS-0125 |
| `backend-api` | no | — |

### Surface area

- Endpoints: none
- Entities / Flyway / configuration: none

### Architecture review

No architecture change: presentation only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
