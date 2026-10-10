# Audit log paginated server-side with a shared pagination footer (slice 1 of #1340)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1340 |
| Use Case | RF-44 – Registro de auditoría; RNF-03 – Tiempo de respuesta |
| Branch | `fix/1340_audit_log_pagination` |
| Gate 1 status | draft |

## Objetivo

`apiGetPaged` asks Spring page endpoints for `size=1000` and drops `totalElements`, so list screens silently hide every row after the 1000th. The audit log, the fastest-growing table (over 3000 rows on the dev DB), lost most of its history and rendered 1000 animated rows. This first slice adds the shared paging pieces and moves `/dashboard/auditoria` to server-side pages. People, managements, budgets and deeds follow in later slices.

## What Changes

- `lib/api-client.ts`: `apiGetPage(path, { page, size, sort, params })` returns the `SpringPage` with totals, and normalises the audit-log `page` field to `number`.
- New `components/shared/Pagination.tsx`: a labelled `nav` with the "x–y of N" status (`aria-live`), "Page p of n", first/previous/next/last and 20/50/100 rows per page.
- `DataTable`: optional `pagination` and `isFetching` props; `aria-busy` on the table while a page loads.
- `useAuditoria({ page, size, module })`: one page sorted `date,desc`, `keepPreviousData`.
- `/dashboard/auditoria`: page, size and module in the URL (`?page=&size=&module=`); the module filter is sent to the server (options from `lib/audit-modules.ts`, the backend's module names); the text search is labelled as searching this page.
- i18n `common.pagination.*` and `auditoria.moduleFilter` (es/en); the module select gets an accessible name.
- Vitest `pagination.test.tsx`, `audit-log-pagination.test.tsx`; Playwright `TS-0104`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A paged list shows the real total and reaches every row | #1340, RF-44 | Made explicit |
| The audit log is never loaded whole | #1340, RNF-03 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-list-pagination`: Lists backed by growing tables are paged server-side with a shared footer.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | api-client, Pagination, DataTable, useAuditoria, auditoria page, messages |
| `testing` | yes | Playwright TS-0104 |
| `backend-api` | no | GET /audit-log already pages and filters by module |

### Surface area

- Route: /dashboard/auditoria (query params page, size, module)
- API: GET /api/v1/audit-log?page&size&sort=date,desc&module (unchanged contract)

### Architecture review

No architecture change: a shared presentational component and one API helper. `apiGetPaged` stays for the other lists until their slices land.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
