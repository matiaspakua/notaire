# People list paginated server-side (slice 2 of #1340)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1340 |
| Use Case | CU18 – Consultar persona; CU54 – Listar clientes; CU61 – Buscar persona o cliente |
| Branch | `fix/1340_people_pagination` |
| Gate 1 status | draft |

## Objetivo

`/dashboard/personas` loaded `GET /people?size=1000`, so on the dev DB (more than 1000 people) everyone after the 1000th was unreachable from the list, and the duplicate-document toast could not link to an existing person outside those 1000 (Playwright `Dedup-GW02` failed on it). This slice moves the people list to server pages with the shared footer from slice 1.

## What Changes

- `usePersonasPage({ page, size })`: one page of `GET /people`, sorted `idPerson,desc` (newest first), `keepPreviousData`; `fetchPersona(id)` reads `GET /people/{id}`.
- New `hooks/useUrlPagination.ts`: page and size in the URL (`?page=&size=`, sizes 20/50/100), shared by the next slices.
- `/dashboard/personas`: the table shows the current server page with the `Pagination` footer and the real total; a search still uses `GET /people/search` and lists every match without the footer.
- `presentPersonaSaveError`: optional `loadPersona`; the 409 duplicate toast keeps its "Ver persona existente" link when the existing person is not on the loaded page and loads it by id.
- New `useClampPage`: a page past the end (stale link, last rows deleted) moves to the last page instead of an empty table.
- `globals.css`: `[data-sonner-toaster] { pointer-events: auto }`. An open Radix dialog sets `pointer-events: none` on `body`, so toast actions were unclickable while a form was open (the duplicate link could be seen but not used).
- Vitest `personas-pagination.test.tsx`; Playwright `TS-0110`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A paged list shows the real total and reaches every row | #1340, RF-44 | Made explicit |
| A person just created is on the first page of the list | #1340, CU17 | New |
| The duplicate-document toast always links to the existing person | #835, CU17 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-list-pagination`: Lists backed by growing tables are paged server-side with a shared footer.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | usePersonas, useUrlPagination, personas page, persona-save-error |
| `testing` | yes | Playwright TS-0110 |
| `backend-api` | no | GET /people already pages with @ParameterObject page/size/sort |

### Surface area

- Route: /dashboard/personas (query params page, size)
- API: GET /api/v1/people?page&size&sort=idPerson,desc, GET /api/v1/people/{id} (unchanged contract)

### Architecture review

No architecture change. `usePersonas()` (size=1000) stays for the person pickers on gestiones and presupuestos and the dashboard counter; those need a search-as-you-type picker and the count endpoint (#1358) and are later slices.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
