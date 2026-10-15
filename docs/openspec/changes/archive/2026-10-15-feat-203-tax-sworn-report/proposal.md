# Tax sworn-statement PDF (#203)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #203 |
| Use Case | CU50 — Generar Declaración Jurada de Rentas (RF #51, #53, #54) |
| Branch | `cursor/203-tax-sworn-statement-5fcb` |
| Gate 1 status | validated |

## Objetivo

Replace the CU50 tax sworn-statement placeholder with a real period query:
year and month required; optional notary registry and protocol type. Header
shows titular/substitute from recorded substitutions when those rows exist.
Each qualifying deed becomes one row: deed number, operation classification,
price, payment form, cadastral designation, fiscal appraisal, and tax.
Operation classification, payment form, and tax stay `N/A` because the domain
has no approved classification, payment-form, or tax-rate fields — do not
invent legal rates or Rentas copy. Empty periods return HTTP 200 and an
explicit empty-state PDF. Year+month-only Bruno and UI clients keep working.

## What Changes

- `TaxSwornStatementReportBuilder` queries recorded folios for the year
  whose deed date falls in the requested month (optional `idNotary`,
  `idFolioType`).
- Titular/substitute follow the CU25 substitution lookup. Missing values
  are `N/A`.
- Price comes from `registration_drafts.operation_price` when present.
  Cadastral designation and fiscal appraisal come from the procedure
  property on a non-auxiliary protocol.
- Totals sum recorded fiscal appraisals and operation prices only. Tax
  total stays `N/A`.
- Empty period: 200 + empty-state PDF. Not 404. No invented legal footer.
- Optional filters so existing year+month callers stay valid.
- UI adds year/month testIds and optional notary/protocol; protocol
  filename aligns with `ddjj_rentas_{year}_{month}.pdf`.
- Playwright adds CU50-GW01 with `seedProtocolReportPeriod`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Inputs: month + year + registry + protocol | CU50, RF #53 | Made explicit (year/month required; registry/protocol optional) |
| Header: titular/substitute and registration | CU50 | New (substitute only when a substitution row exists) |
| Rows: deed, operation, price, payment form, cadastral, fiscal, tax | CU50 | New |
| Operation / payment form / tax not invented | Owner legal pending | Made explicit (`N/A`) |
| Empty period notifies the user (200 + empty-state PDF) | CU50 4.1 | Made explicit |
| Year+month-only clients keep working | existing Bruno/UI | Made explicit |

## Capabilities

### New Capabilities

- `tax-sworn-report`: CU50 query, header, rows, empty period, optional filters, UI/E2E.

### Modified Capabilities

- `pdf-report-design-system`: CU50 consumes the shared renderer with business rows.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | builder, service, controller params, tests, OpenAPI |
| `frontend` | yes | optional filters; hook query string; filename unify |
| `testing` | yes | CU50 Playwright download |
| `docs` | yes | CU50, REST, SAD, CHANGELOG, OpenSpec |
| `contracts` | no | same path; optional query params only |

### Surface area

- Endpoint: `GET /api/v1/reports/tax-sworn-statement?year=&month=&idNotary=&idFolioType=`
- Flyway: none
- Authorization: unchanged

### Architecture review

Keep `ReportController → ReportService → TaxSwornStatementReportBuilder → ReportRenderer`.
Do not grow `ReportDocumentFactory`. Do not invent tax formulas.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU50 – Generar Declaración Jurada de Rentas.md` | Implemented behavior, empty period, `N/A` tax |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | Optional filters |
| `docs/200-architecture/201-SAD/pdf-report-design-system.md` | CU50 no longer a placeholder |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | CU50 download covered |
| `docs/300-development/303-testing/INVENTORY.md` | regenerated |
| `CHANGELOG.md` | Unreleased #203 |

## Out of Scope

- Owner legal footer, signature, or tax-rate tables
- Invented compraventa/remate/payment-form classifications
- Object-level report authorization
- Print-quality logo
- PDF/UA-1 claim
- CU15/CU80/CU08/CU82 legal-form slices
