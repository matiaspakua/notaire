# index-book-report — delta

## Purpose

CU24 index book: recorded deeds for a year (optional notary registry and
protocol type), one row per involved person, sorted by name.

## ADDED Requirements

### Requirement: Period query and row content

The system SHALL generate an index-book PDF for a required `year`. Optional
`idNotary` and `idFolioType` narrow the period to one notary registry and
protocol type. Year-only requests SHALL keep working. Each qualifying deed
SHALL contribute one row per involved person. Involved people SHALL be the
procedure `personList` plus the budget client when that list is empty.
Columns SHALL be person name, procedure name, deed number, first folio
(minimum folio number among matching folios of that deed), and signing
day/month from `deed_date`. Rows SHALL be sorted by normalized last name then
first name.

#### Scenario: Sorted rows from recorded deeds

- **WHEN** the period contains two recorded deeds with involved people Peña and
  Álvarez
- **THEN** extracted text contains both names, their procedure names, deed
  numbers, first folio numbers, and signing day/month, and Álvarez appears
  before Peña

#### Scenario: First folio is the minimum matching number

- **WHEN** one deed has folios 40 and 12 in the requested period
- **THEN** the row shows first folio 12

#### Scenario: Budget client is used when personList is empty

- **WHEN** a recorded deed has a procedure whose `personList` is empty and
  whose budget has a client
- **THEN** the row uses that client name

#### Scenario: Year-only request includes every matching registry

- **WHEN** the caller sends only `year`
- **THEN** deeds from every notary and protocol in that year are included

#### Scenario: Optional filters exclude other registries

- **WHEN** the caller sends `idNotary` or `idFolioType`
- **THEN** only folios of that notary and/or protocol type are included

### Requirement: Empty period notification

When no recorded deed matches the period, the system SHALL return HTTP 200
and a parseable PDF that still shows the title, applied filters, and an
explicit empty-state message. It SHALL NOT invent a 404 and SHALL NOT invent
Owner legal footer or signature language.

#### Scenario: Empty year

- **WHEN** no folio with a linked deed exists for the requested year
- **THEN** the response is 200 `application/pdf` and extracted text contains
  `No deeds were recorded for the requested period.`

### Requirement: UI and download contract

The reports and protocol screens SHALL collect year and may collect notary
registry and protocol type using existing message-catalog labels. Download
SHALL call `GET /api/v1/reports/index-book` with `year` and any selected
filters, return `application/pdf`, and use filename `libro_indice_{year}.pdf`.

#### Scenario: Reports card downloads the index book

- **WHEN** an authenticated user seeds a protocol period and downloads the
  index book from `/dashboard/reports`
- **THEN** the browser receives a PDF whose suggested filename matches
  `libro_indice_*.pdf`, the file starts with `%PDF-`, and extracted text
  contains the seeded person name
