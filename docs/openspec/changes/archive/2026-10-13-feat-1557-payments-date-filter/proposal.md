# Payments: inclusive date-range filter on the list (#1557)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1557 |
| Use Case | CU47 – Consultar Pagos |
| Branch | `feat/1557_payments_date_filter` |
| Gate 1 status | draft |

## Objetivo

CU47 lets the user consult payments by date. The payments page had no date filter, and `GET /payments/date` had no UI caller (#1250 bucket D). Owner answered Q5: yes, add it.

## What Changes

- `PaymentController` `/payments/date`: an inverted range (`startDate` after `endDate`) is a 400 `BusinessValidationException`; a malformed date stays a 400 via `GlobalExceptionHandler`. Documented as both days inclusive (the repository's `BETWEEN` on a DATE column).
- `usePaymentsByDate(from, to)`; the payments page gets Desde/Hasta inputs kept in the URL (`?from=&to=`). Without a budget it calls the server; with a budget it narrows that budget's payments on the client, using the same calendar-day parsing as the Fecha column. An inverted range shows an inline error, no request, no rows.
- Allowlist: `GET /api/v1/payments/date` removed. Bruno `payments/06a` (inverted → 400). i18n `pagos.consulta.desde/hasta/rangoInvalido`.
- TS-0014 CU47-GW01 implemented; feature-gap skips 14 → 13 (test, E2E-TEST-MAPPING, living spec).
- Tests: `PaymentControllerTest` (+2), Vitest `payments-date-filter.test.tsx` (6). CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A date range is inclusive at both ends and `from` ≤ `to` | CU47 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `payment-consult`: Consulting payments by budget and date.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | controller validation, OpenAPI |
| `frontend` | yes | page, hook, i18n, types |
| `testing/e2e` | yes | TS-0014 |
| `testing/tests` | yes | skip inventory |
| `contracts` | yes | allowlist |

### Surface area

- Endpoint `GET /payments/date`: new 400 for an inverted range (was 200 with an empty list); no schema change
- Entities / Flyway / configuration: none

### Architecture review

The controller validates the range (input rule); the existing port and repository query are unchanged.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | CU47-GW01 resolved |
| `docs/openspec/specs/e2e-feature-gap-skip-tracking/spec.md` | count 13 |
| `backend-api/api-test/COVERAGE.md` | payments row |
