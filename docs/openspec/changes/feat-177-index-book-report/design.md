# Design

## Context

Slice 1 landed the shared PDF design system. CU24 still renders a one-page
placeholder. Issue #177 / CU24 requires a real index book: year, notary
registry, and protocol type; people sorted by name; procedure, deed number,
first folio, signing day/month. Empty periods must notify the user.

## Goals / Non-Goals

Goals: query recorded folios; assemble person rows; optional notary/protocol
filters; empty-state 200 PDF; keep year-only clients; UI fields with existing
i18n; Bruno year-only plus Playwright seed/download/text.

Non-goals: Owner legal footer/signature; CU25/CU50 content; object-level
authorization; print logo; PDF/UA-1 claim; inventing 404 for an empty year.

## Decisions

- Dedicated `IndexBookReportBuilder` (SRP). Do not grow `ReportDocumentFactory`.
- Year is required. `idNotary` and `idFolioType` are optional query params so
  Bruno and the current year-only UI keep working.
- A folio qualifies when it has a linked deed. Tests do not need a full
  signing workflow; a recording date on the deed is enough.
- People: `procedure.personList`, then budget `fkIdPerson` if that list is
  empty. Deduplicate by person id / normalized name.
- First folio = minimum folio `number` among matching folios of that deed.
- Signing day/month = `dd/MM` from `deed_date` via `ReportLocaleFormats`
  (handles `java.sql.Date`).
- Empty message is the English operational line already used by other
  reports: `No deeds were recorded for the requested period.`
- UI reuses `common.year`, `administracion.folios.fields.escribano`, and
  `administracion.folios.fields.tipo`. No new legal strings.
- Playwright helper `seedProtocolReportPeriod` creates notary, protocol type,
  folio, deed, procedure, and people through public APIs.

## Riesgos / Trade-offs

- Optional filters change the OpenAPI parameter list; year-only callers stay
  compatible.
- `Deed.procedureList` and `Procedure.personList` are LAZY; the builder runs
  inside `@Transactional(readOnly = true)` and uses `ProcedureRepository`.
- Unskipping CU24 reduces the documented #1146 skip inventory by one.
- Board/label writes for #177 return 403. Owner Continue on the existing issue.

## Testing Strategy

Write failing tests first: `IndexBookReportBuilderTest` (sort, first folio,
budget fallback, filters, empty state), `ReportLocaleFormatsTest` day/month,
`IndexBookReportIntegrationTest` (seeded H2 PDF text), controller/year-only
regression, Vitest hook/page URL, Playwright CU24 download. Observe red, then
implement.

## Regression Strategy

`mvn test -pl backend-api -Dtest=IndexBookReportBuilderTest,IndexBookReportIntegrationTest,ReportLocaleFormatsTest,ReportsUseCaseIntegrationTest,AdditionalControllersTest,ReportServicePaymentReceiptTest`
then targeted frontend Vitest and Playwright CU24. Pipeline/preflight before push.

## Playwright Strategy

Unskip CU24-GW01. `seedProtocolReportPeriod` seeds unique people/deed/folio.
Navigate `/dashboard/reports`, fill year (and optional filters when asserted),
`page.waitForEvent("download")` on `btn-download-index-book`, assert filename,
`%PDF-`, non-trivial size, and extracted text contains the seeded last name.

## Deployment Strategy

Backend + frontend. No schema migration. Rollback is reverting the PR.

## Rollback Strategy

Revert the merge. Year-only placeholder behavior returns. No Flyway step.
