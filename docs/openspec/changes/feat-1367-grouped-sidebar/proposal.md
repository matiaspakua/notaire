# Group the sidebar into six task-based sections and derive the dashboard grid from it (#1367)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1367 |
| Use Case | RNF-10 – Seguimiento del trabajo sobre ventanas; RNF-06 – Usabilidad; CU78 – Security and Compliance (admin-only navigation) |
| Branch | `feat/1367_grouped_sidebar` |
| Gate 1 status | draft |

## Objetivo

The sidebar listed 22 links in one flat, scrolling list with no headings, so related screens (the five document screens, the testimony and copy screens) were not adjacent, and the dashboard module grid kept its own hand-picked array with a different order. The Owner approved six sections on 2026-10-09. The sidebar now shows those sections, each collapsible and remembered per browser, and the dashboard grid is built from the same config.

## What Changes

- `lib/dashboard-nav.ts`: `DASHBOARD_NAV_GROUPS` (Clientes, Trámites, Documentación, Escrituras y protocolo, Gestión, Administración) plus `DASHBOARD_NAV_HOME`; `DASHBOARD_NAV_ITEMS` and `DASHBOARD_MODULES` (the `featured` items) are derived from it; `isNavItemActive` matches a route or its sub-routes only.
- `AppSidebar`: Inicio, then one `role=group` section per group with an `<h2>` holding a disclosure button (`aria-expanded`, `aria-controls`). Collapsed sections persist in `localStorage` (`notaire.sidebar.collapsed`); the section holding the current route opens on load and when the route moves into it. Sections with no visible items (Administración for non-admins) are not rendered.
- `app/dashboard/page.tsx`: the module grid renders `DASHBOARD_MODULES` (same 14 cards, now in sidebar order) inside `data-testid=dashboard-modules`; its private `modules` array is gone.
- Auditoría is `adminOnly` in the nav, matching `ADMIN_ROUTE_PREFIXES`; non-admins no longer see a link that redirects them to `?forbidden=1`.
- Active-link fix: `/dashboard/documentos-necesarios` no longer also highlights Documentos (the old `startsWith` matched the prefix).
- i18n: new `navGroups` namespace in es/en (the `navigation` values stay flat strings).
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Sidebar sections and membership: Clientes (Personas, Inmuebles); Trámites (Gestiones, Presupuestos, Pagos); Documentación (5 document screens); Escrituras y protocolo (Escrituras, Testimonios, Movimientos, Copias, Minutas, Protocolo); Gestión (Reportes, Auditoría, Suplencias, Items); Administración | Owner decision 2026-10-09 on #1367; Items placed under Gestión (not in the written list) | New |
| Admin-only nav items are exactly the admin-guarded routes | `ADMIN_ROUTE_PREFIXES` (#1052) | Made explicit |
| The dashboard grid and the sidebar come from one config | #1367 acceptance criteria | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `dashboard-navigation`: The dashboard sidebar groups its links into task-based sections shared with the dashboard module grid.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | nav config, sidebar, dashboard grid, messages |
| `testing/e2e` | yes | TS-0121 |
| `backend-api` | no | — |

### Surface area

- Endpoints: none
- Entities / Flyway / configuration: none
- Browser storage: `localStorage['notaire.sidebar.collapsed']` (array of section keys)

### Architecture review

No architecture change: client-side presentation only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Changed entry |
| `frontend/messages/{es,en}.json` | navGroups namespace |
