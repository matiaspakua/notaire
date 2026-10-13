# Monthly sworn-statement PDF (#178)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #178 |
| Use Case | CU25 — Generar Declaración Jurada del mes (RF #51, #52, #54) |
| Branch | `cursor/178-monthly-sworn-statement-5fcb` |
| Gate 1 status | validated |

## Objetivo

Replace the CU25 monthly sworn-statement placeholder with a real period query:
year and month required; optional notary registry and protocol type. Header
shows titular/substitute from recorded substitutions when those rows exist.
Each qualifying deed becomes one row: deed number, first folio, day of month,
acts and parties, annex, cadastral/fiscal/price when a principal-protocol
property or registration-draft price exists. Tax and rate stay `N/A` because
the domain has no approved tax table — do not invent legal rates. Empty
periods return HTTP 200 and an explicit empty-state PDF. Year+month-only
Bruno and UI clients keep working.

## What Changes

- `MonthlySwornStatementReportBuilder` queries recorded folios for the year
  whose deed date falls in the requested month (optional `idNotary`,
  `idFolioType`).
- Titular is the filtered notary (or the distinct notaries in the period).
  Substitute comes from a substitution that covers the month for that
  titular; otherwise `N/A`.
- First folio is the minimum folio number. Day is `dd` from `deed_date`.
  Acts are procedure type names. Parties are `personList` or budget client.
- Annex is `N/A` (no annex entity). Tax/rate are `N/A` (no Owner rate table).
  Cadastral designation and fiscal appraisal come from the procedure
  property on a non-auxiliary protocol. Price comes from
  `registration_drafts.operation_price` when present.
- Totals sum recorded fiscal appraisals and operation prices only.
- Empty period: 200 + empty-state PDF. Not 404. No invented legal footer.
- Optional filters so existing year+month callers stay valid.
- UI adds optional notary/protocol using existing i18n; protocol filename
  aligns with `ddjj_mensual_{year}_{month}.pdf`.
- Playwright unskips CU25 with `seedProtocolReportPeriod`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Inputs: month + year + registry + protocol | CU25, RF #52 | Made explicit (year/month required; registry/protocol optional) |
| Header: titular/substitute and registration | CU25, RF #52 | New (substitute only when a substitution row exists) |
| Rows: deed, first folio, day, acts, parties, annex, values | CU25 | New |
| Principal-protocol property cadastral/fiscal/price | RF #52 | New (price from registration draft only) |
| Tax/rate not invented | Owner legal pending | Made explicit (`N/A`) |
| Empty period notifies the user (200 + empty-state PDF) | CU24 pattern / CU50 4.1 | Made explicit |
| Year+month-only clients keep working | existing Bruno/UI | Made explicit |

## Capabilities

### New Capabilities

- `monthly-sworn-report`: CU25 query, header, rows, empty period, optional filters, UI/E2E.

### Modified Capabilities

- `pdf-report-design-system`: CU25 consumes the shared renderer with business rows.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | builder, service, controller params, tests, OpenAPI |
| `frontend` | yes | optional filters; hook query string; filename unify |
| `testing` | yes | unskip CU25 Playwright |
| `docs` | yes | CU25, REST, SAD, CHANGELOG, OpenSpec |
| `contracts` | no | same path; optional query params only |

### Surface area

- Endpoint: `GET /api/v1/reports/monthly-sworn-statement?year=&month=&idNotary=&idFolioType=`
- Flyway: none
- Authorization: unchanged

### Architecture review

Keep `ReportController → ReportService → MonthlySwornStatementReportBuilder → ReportRenderer`.
Do not grow `ReportDocumentFactory`. Do not invent tax formulas.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU25 – Generar Declaración Jurada del mes.md` | Implemented behavior, empty period, `N/A` tax |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | Optional filters |
| `docs/200-architecture/201-SAD/pdf-report-design-system.md` | CU25 no longer a placeholder |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | CU25 unskipped |
| `CHANGELOG.md` | Unreleased #178 |

## Out of Scope

- CU50 tax sworn-statement content
- Owner legal footer, signature, or tax-rate tables
- Object-level report authorization
- Print-quality logo
- PDF/UA-1 claim
