# Audit log: filter by user and inclusive date range, server-side (#1545)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1545 |
| Use Case | CU23 – Ver registro de actividades de usuario; CU73 |
| Branch | `feat/1545_audit_user_date_filter` |
| Gate 1 status | draft |

## Objetivo

CU23 asks the notary to pick a user and a date range and see that user's activity. `/dashboard/audit` only paged and filtered by module. `useAuditByUser` had no caller, and the API ignored `idUser` whenever `module` was set.

## What Changes

- Backend `GET /api/v1/audit-log`: new optional `from`/`to` (yyyy-MM-dd, inclusive calendar days, JVM/business zone). Module, idUser and the range combine through a JPA Specification; `from > to` and a malformed date answer 400. Additive OpenAPI change; `api.generated.ts` regenerated.
- Frontend audit page: a user selector (`GET /users`) and Desde/Hasta inputs, kept in the URL with page, size and module. Changing a filter resets the page. An inverted range shows an inline error and isn't sent.
- `useAuditByUser` removed. `GET /audit-log/user/{idUser}` stays as API-only (allowlist bucket A).
- Bruno audit-records 05 (filters) and 06 (inverted range → 400); COVERAGE row.
- Tests: JUnit `AuditLogUserDateFilterIntegrationTest` (5), `AuditRecordControllerTest` adapted; Vitest `audit-user-date-filter.test.tsx` (4); Playwright TS-0016 `CU23-GW01` implemented (was skipped). CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Date range is inclusive of both calendar days | CU23 | New |
| Filters combine (module AND user AND range) | CU23 | Fix (module used to override user) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `audit-log-filter`: Filtering the audit log by user and date range.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | controller, service, port, adapter, OpenAPI, Bruno |
| `frontend` | yes | audit page, hook, i18n, generated types |
| `contracts` | yes | allowlist bucket A entry |
| `testing/e2e` | yes | TS-0016 |

### Surface area

- Endpoints: GET /api/v1/audit-log gains optional `from`/`to` (additive)
- Entities / Flyway / configuration: none

### Architecture review

The service converts days to [start, next-day start) instants and the persistence adapter builds a Specification; the controller stays thin.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
| `backend-api/api-test/COVERAGE.md` | audit-records row |
| `contracts/api-reachability-allowlist.yaml` | GET /audit-log/user/{idUser} as bucket A |
