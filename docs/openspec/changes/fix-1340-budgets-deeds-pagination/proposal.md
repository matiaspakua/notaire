# Budgets and deeds lists paginated server-side (slice 5 of #1340)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1340 |
| Use Case | CU01 – Preparar presupuesto; CU60 – Buscar presupuestos por estado; CU06 – Preparar escritura; CU07 – Consultar escritura |
| Branch | `fix/1340_budgets_deeds_pagination` |
| Gate 1 status | draft |

## Objetivo

`/dashboard/presupuestos` and `/dashboard/escrituras` loaded `size=1000`: on the dev database (more than 1000 budgets) budgets after the 1000th were unreachable, and the dashboard counted at most 1000 budgets. Both filters were also broken: the status filter sent `?estado=` and the deed search `?numero=`, while the backend reads `status` and `number`, so each answered every row unfiltered.

## What Changes

- `usePresupuestosPage` (`sort=idBudget,desc`) and `useEscriturasPage` (`sort=idDeed,desc`): one page of `GET /presupuestos` and `GET /escrituras`, `keepPreviousData`.
- Both screens show the server page with the shared `Pagination` footer, the real total and `page`/`size` in the URL (`useUrlPagination`, `useClampPage`).
- Budgets: the status filter calls `GET /presupuestos/buscar?status=` (every budget of that status, no footer); a number in the search box is read with `GET /presupuestos/{id}`, so it is found on any page; a client name filters the rows shown.
- Deeds: the number search calls `GET /escrituras/buscar?number=` (exact match, no footer).
- Dashboard: the budgets counter shows `totalElements`.
- E2E robustness: `presupuesto-plantilla` GW01 uses its own uniquely named procedure type instead of `.last()` (it raced with parallel tests).
- Vitest `budgets-deeds-pagination.test.tsx`; Playwright `TS-0113`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A paged list shows the real total and reaches every row | #1340, RF-44 | Made explicit |
| The status filter lists only budgets in that status | CU60 | Made explicit (was broken) |
| The deed number search lists only the deed with that number | CU07 | Made explicit (was broken) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-list-pagination`: the budgets and deeds lists join the server-paged lists.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | usePresupuestos, useEscrituras, presupuestos page, escrituras page, dashboard page |
| `testing` | yes | Playwright TS-0113, presupuesto-plantilla GW01 |
| `backend-api` | no | GET /presupuestos and GET /escrituras already page with @ParameterObject page/size/sort |

### Surface area

- Routes: /dashboard/presupuestos, /dashboard/escrituras (query params page, size), /dashboard
- API (unchanged contract): GET /api/v1/presupuestos?page&size&sort, GET /api/v1/presupuestos/buscar?status, GET /api/v1/presupuestos/{id}, GET /api/v1/escrituras?page&size&sort, GET /api/v1/escrituras/buscar?number

### Architecture review

No architecture change. Backend gaps, reported and not changed here: `GET /presupuestos/buscar` and `GET /escrituras/buscar` return unpaged lists, and there is no budget search by client name (a name filters only the rows shown). `usePresupuestos()` / `useEscrituras()` (size=1000) stay for the selects of gestiones, pagos, testimonios, minutas and folios: later slices with a searchable picker.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
