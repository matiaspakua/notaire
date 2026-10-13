# monthly-sworn-report — delta

## Purpose

CU25 monthly sworn statement: recorded deeds for a year and month (optional
notary registry and protocol type), header titular/substitute, one row per
deed.

## ADDED Requirements

### Requirement: Period query and row content

The system SHALL generate a monthly sworn-statement PDF for required `year`
and `month`. Optional `idNotary` and `idFolioType` narrow the period.
Year+month-only requests SHALL keep working. Each qualifying deed SHALL
contribute one row with deed number, first folio (minimum matching folio
number), day of month from `deed_date`, acts (procedure type names),
parties (`personList` or budget client), annex, cadastral designation,
fiscal appraisal, operation price, and tax. Tax, rate, and annex SHALL be
the localized `N/A` fallback unless a recorded domain value exists. Operation
price SHALL come from a registration draft when present. Cadastral and
fiscal appraisal SHALL come from the procedure property on a non-auxiliary
protocol. The system SHALL NOT invent a tax rate.

#### Scenario: Month deeds appear with day and first folio

- **WHEN** the period contains a recorded deed dated 15 March with folio 7
- **THEN** extracted text contains the deed number, first folio 7, day 15,
  the procedure name, and the party name

#### Scenario: Other months are excluded

- **WHEN** a deed is recorded in June of the same year
- **THEN** a March request does not include that June deed number

#### Scenario: Tax and annex stay N/A

- **WHEN** the deed has no tax table and no annex entity
- **THEN** those cells are `N/A`

### Requirement: Titular and substitute header

When a notary filter is applied, the header SHALL show that notary as
titular (name and registration). If a substitution covers the requested
month for that titular, the header SHALL show the substitute name.
Otherwise the substitute value SHALL be `N/A`.

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
Download SHALL call `GET /api/v1/reports/monthly-sworn-statement` with
`year`, `month`, and any selected filters, return `application/pdf`, and use
filename `ddjj_mensual_{year}_{month}.pdf`.

#### Scenario: Reports card downloads the monthly sworn statement

- **WHEN** an authenticated user seeds a protocol period and downloads the
  monthly sworn statement from `/dashboard/reports`
- **THEN** the browser receives a PDF whose suggested filename matches
  `ddjj_mensual_*.pdf`, the file starts with `%PDF-`, and extracted text
  contains the seeded person name
