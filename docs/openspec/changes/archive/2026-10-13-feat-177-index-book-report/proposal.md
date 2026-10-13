# Index book business PDF (#177)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #177 |
| Use Case | CU24 — Generar libro de índices (RF #48, #49, #50) |
| Branch | `cursor/177-index-book-report-5fcb` |
| Gate 1 status | draft |

## Objetivo

Replace the CU24 index-book placeholder with a real period query and table:
year (required) plus optional notary registry and protocol type; one row per
involved person; alphabetical name order; deed number, first folio, and
signing day/month. Empty periods return HTTP 200 and an explicit empty-state
PDF. Year-only Bruno and UI clients keep working. This is Slice 2 of the PDF
modernization plan. No Owner legal form text is invented.

## What Changes

- `IndexBookReportBuilder` queries recorded folios for the year (optional
  `idNotary`, `idFolioType`), groups by deed, and emits person rows.
- Involved people come from `procedure.personList`, with the budget client as
  fallback. Sort key is normalized last name then first name (`es-AR`).
- First folio is the minimum folio number among matching folios of the deed.
  Signing day/month comes from `deed_date` via `ReportLocaleFormats`.
- Empty period: 200 + shared renderer empty state
  (“No deeds were recorded for the requested period.”). Not 404.
- `GET /api/v1/reports/index-book` keeps `year` required; `idNotary` and
  `idFolioType` are optional so existing year-only callers stay valid.
- Reports and protocol UI collect year plus optional notary and protocol using
  existing i18n keys. No new legal copy.
- Bruno year-only fixture stays. Playwright unskips CU24 with
  `seedProtocolReportPeriod` and download / `%PDF-` / extracted-text checks.
- CHANGELOG Unreleased for #177. CU24 and REST docs updated.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Inputs: year + notary registry + protocol type | CU24, RF #48–50 | Made explicit (year required; registry/protocol optional filters) |
| Rows: person, procedure, deed number, first folio, signing day/month | CU24 | New |
| Alphabetical order by involved person name | CU24 | New |
| Empty period notifies the user (PDF empty state, HTTP 200) | CU24 4.1 | Made explicit (do not invent 404) |
| Year-only clients keep working | existing Bruno/UI | Made explicit |
| No invented Owner legal footer/signature | Owner decision pending | Out of scope |

## Capabilities

### New Capabilities

- `index-book-report`: CU24 query, row assembly, empty period, optional filters, UI/E2E.

### Modified Capabilities

- `pdf-report-design-system`: CU24 now consumes the shared renderer with business rows.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | builder, service, controller params, Folio query, tests, OpenAPI |
| `frontend` | yes | optional notary/protocol fields; hook query string |
| `testing` | yes | `seedProtocolReportPeriod`; unskip CU24 Playwright |
| `docs` | yes | CU24, REST reference, SAD pointer, CHANGELOG, OpenSpec |
| `contracts` | no | same path; optional query params only |

### Surface area

- Endpoint: `GET /api/v1/reports/index-book?year=&idNotary=&idFolioType=`
- Flyway: none
- Authorization: unchanged (authenticated-only; matrix is a later slice)

### Architecture review

Keep `ReportController → ReportService → IndexBookReportBuilder → ReportRenderer`.
Do not grow `ReportDocumentFactory`. PDFBox stays in the adapter.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU24 – Generar libro de índices.md` | Implemented behavior, empty period, UI/API |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | Optional filters |
| `docs/200-architecture/201-SAD/pdf-report-design-system.md` | CU24 no longer a later-slice placeholder |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | CU24 unskipped |
| `CHANGELOG.md` | Unreleased #177 |

## Out of Scope

- CU25 / CU50 sworn-statement content
- Owner legal footer or signature language
- Object-level report authorization
- Print-quality logo approval
- PDF/UA-1 claim

