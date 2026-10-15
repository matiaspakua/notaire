# Design

## Context

Slice 3 shipped CU25. CU50 still renders a one-page placeholder. Issue #203
requires a monthly Rentas sworn statement: month, year, notary registry,
protocol; titular/substitute; one row per deed with operation class, price,
payment form, cadastral, fiscal appraisal, and tax. The domain has no
approved tax-rate table, operation-class, or payment-form entity.

## Goals / Non-Goals

Goals: query recorded folios in the month; header titular/substitute;
deed rows; optional notary/protocol filters; empty-state 200 PDF; keep
year+month-only clients; Playwright seed/download/text.

Non-goals: inventing tax/rate/legal footer or Rentas classifications;
object-level authorization; print logo.

## Decisions

- Dedicated `TaxSwornStatementReportBuilder` (SRP).
- Year and month required. `idNotary` and `idFolioType` optional.
- A folio qualifies when it has a linked deed, `folio.year` matches, and
  `deed_date` falls in the requested month (same period rule as CU25).
- Substitute is looked up only from `substitutions` covering mid-month.
- Operation classification, payment form, and tax are `N/A`. Price is
  `registration_drafts.operation_price` when present. Fiscal appraisal
  and cadastral come from the procedure property on a non-auxiliary
  protocol.
- Totals sum recorded fiscal appraisals and operation prices only.
- Empty message: `No deeds were recorded for the requested period.`
- Landscape page.
- Protocol filename becomes `ddjj_rentas_{year}_{month}.pdf`.
- Playwright reuses `seedProtocolReportPeriod` (March of the seeded year).

## Riesgos / Trade-offs

- Tax and Rentas classification columns stay `N/A` until Owner supplies
  those tables.
- `personList` writes still fail on `person_procedures.version`; E2E uses
  the budget-client fallback.

## Testing Strategy

Write failing tests first: `TaxSwornStatementReportBuilderTest` (month
filter, row content, N/A tax/operation/payment, empty state),
integration seeded H2 PDF text, controller year+month regression, Vitest
hook URL, Playwright CU50 download. Observe red, then implement.

## Regression Strategy

`mvn test -pl backend-api -Dtest=TaxSwornStatementReportBuilderTest,TaxSwornStatementReportIntegrationTest,ReportsUseCaseIntegrationTest,AdditionalControllersTest,ReportServicePaymentReceiptTest`
then targeted frontend Vitest and Playwright CU50.

## Playwright Strategy

Add CU50-GW01. Reuse `seedProtocolReportPeriod` (March of the seeded year).
Navigate `/dashboard/reports`, fill year and month 3,
`page.waitForEvent("download")` on `btn-download-tax-sworn`, assert
filename `ddjj_rentas_*.pdf`, `%PDF-`, and extracted text contains the
seeded last name.

## Deployment Strategy

Backend + frontend. No schema migration. Rollback is reverting the PR.

## Rollback Strategy

Revert the merge. Year+month placeholder behavior returns. No Flyway step.
