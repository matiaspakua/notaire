# Suplencias and Reportes reachable from navigation; remove duplicate admin pages

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1058 |
| Use Case | **CU22** Registrar Suplencia; **CU59** Consultar Suplencias; **CU24**/**CU25**/**CU50** Reportes y DDJJ; **CU23** Ver registro de actividades |
| Branch | `cursor/fix-1058-suplencias-reportes-nav-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal stockpile); implement after queue ahead of this pack |

## Objetivo

Operators cannot reach `/dashboard/suplencias` or `/dashboard/reportes` from the
sidebar, dashboard home, or administración index — only hard `page.goto` in E2E
opens them. Duplicate admin pages (`administracion/items`,
`administracion/auditoria`) fork the canonical `/dashboard/items` and
`/dashboard/auditoria` routes. Add role-appropriate nav links, collapse
duplicates to one canonical route each, and make Playwright navigate via the UI.

## What Changes

- Add Suplencias and Reportes entries to `AppSidebar` (and dashboard home modules
  where appropriate) with i18n keys in `frontend/messages/{es,en}.json`.
- Choose role visibility: Suplencias/Reportes available to authenticated users
  who already can open those pages today (not admin-only unless product rules
  require); keep Administración `adminOnly`.
- Remove duplicate pages under `dashboard/administracion/items` and
  `dashboard/administracion/auditoria` (or replace with redirects to canonical
  `/dashboard/items` and `/dashboard/auditoria`).
- Update administración index / any leftover links so Items and Auditoría point
  at canonical routes (Auditoría already on sidebar/home; Items already on
  sidebar/home).
- Update E2E (TS-0070, TS-0017, TS-0020, TS-0060, TS-0043, TS-0091, TS-0024 as
  needed) to reach routes via sidebar/home navigation instead of raw `goto`
  where the AC requires navigation.
- Unit tests for nav config / i18n keys; Playwright asserts link presence.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Authenticated users MUST reach Suplencias from primary navigation | CU22, CU59; #1058 AC | New (nav) |
| Authenticated users MUST reach Reportes from primary navigation | CU24, CU25, CU50; #1058 AC | New (nav) |
| Items and Auditoría MUST have exactly one canonical dashboard route | CU23; #1058 AC | Changed (dedupe) |
| E2E MUST exercise discovery via navigation UI, not only deep links | CU76/testing; #1058 AC | Changed (E2E) |

## Capabilities

### New Capabilities

- `dashboard-nav-canonical-routes`: sidebar/home links for Suplencias and
  Reportes; single canonical routes for Items and Auditoría; E2E via nav.

### Modified Capabilities

- (none under `openspec/specs/` today encode these nav/route rules)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | `AppSidebar`, dashboard home, i18n, delete/redirect duplicate admin pages, E2E + unit tests |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none (existing hooks/pages)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- **BREAKING**: bookmarks to `/dashboard/administracion/items` or
  `.../auditoria` should redirect (301/rewrite or Next redirect) to canonical
  paths — document in CHANGELOG

### Architecture review

Follows existing Next.js app-router dashboard layout and design-system pages.
No new ADR. Prefer thin redirects over maintaining two page implementations.
Keep admin route guard (#1052) behavior for `/dashboard/administracion/**`;
canonical auditoria/items outside that prefix remain as today.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| CU22 / CU59 / CU24 / CU25 / CU50 / CU23 (as needed) | Note UI entry via sidebar/home |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Update routes / nav discovery for affected TS-* |
| `CHANGELOG.md` | fix entry: nav links + duplicate route removal |

## Out of Scope

- New Suplencias/Reportes business features (filters skipped under #1146 stay
  unless separately issued).
- Backend report generation APIs.
- Broader nav IA redesign beyond adding the missing links and deduping the two
  duplicate admin pages.
- #1050 repo hygiene; #976 Vitest floor.
