# Managements list paginated server-side (slice 4 of #1340)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1340 |
| Use Case | CU02 – Iniciar gestión; CU19 – Consultar gestión |
| Branch | `fix/1340_gestiones_pagination` |
| Gate 1 status | draft |

## Objetivo

`/dashboard/gestiones` loaded `GET /gestiones?size=1000`: managements after the 1000th were unreachable, and the dev database (593 managements) rendered every row with its row animation. Leaving that screen outlasted the 10 s navigation wait of the TS-0070 tour (Gestiones → Presupuestos), which failed on every run against the #1394 build. The dashboard also took the "latest management" from the first element of that unsorted list and counted managements with its length (at most 1000).

## What Changes

- `useGestionesPage({ page, size })`: one page of `GET /gestiones`, sorted `idManagement,desc` (newest first), `keepPreviousData`.
- `/dashboard/gestiones`: the table shows the current server page with the shared `Pagination` footer and the real total; page and size live in the URL (`useUrlPagination`, `useClampPage` from slice 2). Filtering by client still uses `GET /gestiones/cliente/{id}` and lists every match without the footer.
- Dashboard: the managements counter shows `totalElements` and the workflow hero follows the newest management (`page=0&size=1`, newest first) instead of loading the list.
- Vitest `gestiones-pagination.test.tsx`; Playwright `TS-0112`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A paged list shows the real total and reaches every row | #1340, RF-44 | Made explicit |
| A management just created is on the first page of the list | #1340, CU02 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-list-pagination`: the managements list joins the server-paged lists.

## Out of scope

- The budget select of the management form and the other `size=1000` selects (documentos, reingreso, documentos de entidades externas): later slices.
- Backend: `GET /gestiones` is already paged; no backend change.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | useGestiones (useGestionesPage), gestiones page, dashboard page |
| `testing` | yes | Playwright TS-0112 |
| `backend-api` | no | GET /gestiones already pages with @ParameterObject page/size/sort |

### Surface area

- Route: /dashboard/gestiones (query params page, size), /dashboard
- API: GET /api/v1/gestiones?page&size&sort=idManagement,desc (unchanged contract)

### Architecture review

No architecture change. `useGestiones()` (size=1000) stays for the gestión selects of documentos, reingreso-documentacion and documentos-entidades-externas; those need a searchable picker and are later slices.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
