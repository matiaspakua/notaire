# pdf-report-design-system — delta

## Purpose

Shared accessible deterministic PDF design system used by every current report.

## ADDED Requirements

### Requirement: Shared print tokens and page anatomy

The PDF adapter SHALL render every `ReportDocument` with A4 portrait by default
(A4 landscape only when identity requests it), 18 mm top/side and 16 mm bottom
margins, the documented type scale, a repeating header (wordmark, report type,
document identifier), and a repeating footer with product version, generated
timestamp in `America/Argentina/Buenos_Aires`, and `Página X de Y`.

#### Scenario: Metadata and language

- **WHEN** a report is rendered
- **THEN** the PDF is parseable, uses the declared page size, has non-empty
  title/producer/creation metadata, and declares language `es-AR`

#### Scenario: Repeating chrome

- **WHEN** a table spans more than one page
- **THEN** every page shows the wordmark header and `Página X de Y`, and
  continuation table pages repeat the table header

#### Scenario: Landscape primitive

- **WHEN** identity requests landscape
- **THEN** the page media box is A4 landscape

### Requirement: Embedded Unicode font and wordmark

Every font used for user or domain text SHALL be the classpath Liberation Sans
family (SIL OFL 1.1), embedded. Spanish and English fixtures SHALL not be
replaced with `?`. The logo SHALL be the text wordmark "Notaire" until an
Owner-approved print asset is present. The 100×100 legacy PNG SHALL not be loaded.

#### Scenario: Spanish English fixtures

- **WHEN** a field contains `Lucía Peña`, `N°`, and `€`
- **THEN** extracted text contains those characters and no `?` substitution
  for them

#### Scenario: Wordmark not legacy logo

- **WHEN** a report is rendered
- **THEN** extracted text contains `Notaire` and the asset catalog reports
  no approved raster logo

### Requirement: Deterministic formatting and clock

Money and dates SHALL use `es-AR` (`$ 1.234,56`, `dd/MM/yyyy`). Generation
time SHALL come from an injected `Clock` and `America/Argentina/Buenos_Aires`.
Null values SHALL render the approved fallback. Empty tables SHALL show the
report-specific empty message.

#### Scenario: Fixed clock

- **WHEN** the renderer is given a fixed clock
- **THEN** the footer timestamp matches that instant in the Buenos Aires zone

#### Scenario: Empty table

- **WHEN** a table has no rows
- **THEN** the PDF still shows the title and the table empty message

### Requirement: Safe text and bounds

User text SHALL be written as PDF text only. Control and bidirectional-override
characters SHALL be neutralized. Long tokens SHALL wrap. Content, rules, and
chrome SHALL stay inside the configured margins for 1, 20, and 100 row fixtures.

#### Scenario: Control characters

- **WHEN** a field contains a newline, tab, or bidi override
- **THEN** those characters are replaced with spaces and generation succeeds

#### Scenario: Structural bounds

- **WHEN** a canonical fixture is rasterized at 144 DPI
- **THEN** page dimensions match A4, header and footer zones have ink, and
  the outer margin strips stay free of content ink

#### Scenario: Performance target

- **WHEN** a 100-row table is rendered
- **THEN** generation finishes in under 3 seconds

### Requirement: Centralized PDF HTTP responses

Successful PDF responses SHALL send `application/pdf`, a safe quoted filename
with no path characters, `Content-Length`, `Cache-Control: private, no-store`,
and `X-Content-Type-Options: nosniff`. Unexpected generation failures on wrapped
report endpoints SHALL return structured `ErrorResponse` JSON.

#### Scenario: Safe headers

- **WHEN** a PDF is returned
- **THEN** the filename is quoted and stripped of path characters and the
  cache and nosniff headers are present

#### Scenario: Structured 500

- **WHEN** report generation throws an unexpected exception
- **THEN** the client receives HTTP 500 with the standard `ErrorResponse` body
