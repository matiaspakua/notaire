# Design

## Context

Slice 2 shipped CU24. CU25 still renders a one-page placeholder. Issue #178
requires a monthly sworn statement: month, year, notary registry, protocol;
titular/substitute; one row per deed with folio, day, acts, parties, and
taxable values. The domain has no approved tax-rate table or annex entity.

## Goals / Non-Goals

Goals: query recorded folios in the month; header titular/substitute;
deed rows; optional notary/protocol filters; empty-state 200 PDF; keep
year+month-only clients; Playwright seed/download/text.

Non-goals: inventing tax/rate/legal footer; CU50 content; object-level
authorization; print logo.

## Decisions

- Dedicated `MonthlySwornStatementReportBuilder` (SRP).
- Year and month required. `idNotary` and `idFolioType` optional.
- A folio qualifies when it has a linked deed, `folio.year` matches, and
  `deed_date` falls in the requested month.
- Substitute is looked up only from `substitutions` covering mid-month for
  the titular. Missing substitute is `N/A`, not invented.
- Tax, rate, and annex are `N/A`. Price is `registration_drafts.operation_price`
  when present. Fiscal appraisal and cadastral come from the procedure
  property on a non-auxiliary protocol.
- Totals sum recorded fiscal appraisals and operation prices only.
- Empty message: `No deeds were recorded for the requested period.`
- Landscape page: many value columns.
- UI reuses existing i18n. Protocol filename becomes `ddjj_mensual_{year}_{month}.pdf`.
- Playwright reuses `seedProtocolReportPeriod` (March of the seeded year).

## Riesgos / Trade-offs

- Tax columns will show `N/A` until Owner supplies a rate table.
- `personList` writes still fail on `person_procedures.version`; E2E uses
  the budget-client fallback.

## Testing Strategy

Write failing tests first: `MonthlySwornStatementReportBuilderTest` (month
filter, row content, substitute, N/A tax, empty state),
`ReportLocaleFormatsTest` day-of-month, integration seeded H2 PDF text,
controller year+month regression, Vitest hook URL, Playwright CU25
download. Observe red, then implement.

## Regression Strategy

`mvn test -pl backend-api -Dtest=MonthlySwornStatementReportBuilderTest,MonthlySwornStatementReportIntegrationTest,ReportLocaleFormatsTest,ReportsUseCaseIntegrationTest,AdditionalControllersTest,ReportServicePaymentReceiptTest`
then targeted frontend Vitest and Playwright CU25. Pipeline/preflight before push.

## Playwright Strategy

Unskip CU25-GW01. Reuse `seedProtocolReportPeriod` (March of the seeded year).
Navigate `/dashboard/reports`, fill year and month 3,
`page.waitForEvent("download")` on `btn-download-monthly-sworn`, assert
filename `ddjj_mensual_*.pdf`, `%PDF-`, and extracted text contains the
seeded last name.

## Deployment Strategy

Backend + frontend. No schema migration. Rollback is reverting the PR.

## Rollback Strategy

Revert the merge. Year+month placeholder behavior returns. No Flyway step.
