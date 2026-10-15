# tax-sworn-report — delta

## Purpose

CU50 tax sworn statement: recorded deeds for a year and month (optional
notary registry and protocol type), header titular/substitute, one row per
deed.

## ADDED Requirements

### Requirement: Period query and row content

The system SHALL generate a tax sworn-statement PDF for required `year`
and `month`. Optional `idNotary` and `idFolioType` narrow the period.
Year+month-only requests SHALL keep working. Each qualifying deed SHALL
contribute one row with deed number, operation classification, payment
form, cadastral designation, fiscal appraisal, operation price, and tax.
Operation classification, payment form, and tax SHALL be the localized
`N/A` fallback unless a recorded domain value exists. The system SHALL
NOT invent a tax rate or Rentas classification.

#### Scenario: Month deeds appear with cadastral and price

- **WHEN** the period contains a recorded deed dated 15 March with a
  principal-protocol property and a registration-draft price
- **THEN** extracted text contains the deed number, cadastral designation,
  and the formatted price

#### Scenario: Other months are excluded

- **WHEN** a deed is recorded in June of the same year
- **THEN** a March request does not include that June deed number

#### Scenario: Tax, operation, and payment form stay N/A

- **WHEN** the deed has no tax table, operation class, or payment-form entity
- **THEN** those cells are `N/A`

### Requirement: Titular and substitute header

When a notary filter is applied, the header SHALL show that notary as
titular. If a substitution covers the requested month, the header SHALL
show the substitute name. Otherwise the substitute value SHALL be `N/A`.

#### Scenario: Substitute from an overlapping substitution

- **WHEN** the titular has a substitution covering the requested month
- **THEN** extracted text contains the substitute last name

### Requirement: Empty period notification

When no recorded deed matches the period, the system SHALL return HTTP 200
and a parseable PDF that still shows the title, applied filters, and an
explicit empty-state message. It SHALL NOT invent a 404 and SHALL NOT invent
Owner legal footer, signature, or tax language.

#### Scenario: Empty month

- **WHEN** no folio with a linked deed exists for the requested year and month
- **THEN** the response is 200 `application/pdf` and extracted text contains
  `No deeds were recorded for the requested period.`

### Requirement: UI and download contract

The reports and protocol screens SHALL collect year and month and may collect
notary registry and protocol type using existing message-catalog labels.
Download SHALL call `GET /api/v1/reports/tax-sworn-statement` with
`year`, `month`, and any selected filters, return `application/pdf`, and use
filename `ddjj_rentas_{year}_{month}.pdf`.

#### Scenario: Reports card downloads the tax sworn statement

- **WHEN** an authenticated user seeds a protocol period and downloads the
  tax sworn statement from `/dashboard/reports`
- **THEN** the browser receives a PDF whose suggested filename matches
  `ddjj_rentas_*.pdf`, the file starts with `%PDF-`, and extracted text
  contains the seeded person name
