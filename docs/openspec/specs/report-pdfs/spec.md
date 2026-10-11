# report-pdfs Specification

## Purpose
PDF reports generated from the current domain model.
## Requirements
### Requirement: Reports from the current schema

The six report endpoints SHALL return an `application/pdf` document built from the current domain model, with the data of the requested aggregate.

#### Scenario: Budget report

- **WHEN** a client requests /api/v1/reports/budget/{id} for an existing budget
- **THEN** the PDF lists the client, procedure types, items, and the CU47 total and pending balance

#### Scenario: Budget report with properties

- **WHEN** a client requests /api/v1/reports/budget-properties/{id}
- **THEN** the PDF also lists cadastral designation, address and fiscal appraisal of the properties of the budget's procedures

#### Scenario: Procedure documents report

- **WHEN** a client requests /api/v1/reports/procedure-document-list with an existing procedure type name
- **THEN** the PDF lists the document types of its template with expiry, validity days and who delivers them

#### Scenario: Management history report

- **WHEN** a client requests /api/v1/reports/management-history/{id}
- **THEN** the PDF lists the management data and its state history, oldest first

#### Scenario: Submitted document expiry report

- **WHEN** a client requests /api/v1/reports/documents-due/{id}
- **THEN** the PDF shows the due date, the days left, the payment status and the management, procedure and client

#### Scenario: Document debt report

- **WHEN** a client requests /api/v1/reports/document-debt?numberManagement=N
- **THEN** the PDF lists the submitted documents of the managements numbered N with amount, payment date or Payment pendiente, and the total owed

### Requirement: Missing aggregates are 404

A report for a budget, procedure type, management, submitted document or management number that does not exist SHALL answer 404 with an `ErrorResponse` body.

#### Scenario: Missing aggregate

- **WHEN** a client requests any of the six reports for an aggregate that does not exist
- **THEN** the response is 404

### Requirement: Readable PDFs

Report PDFs SHALL wrap long values, repeat table headers on every page, number pages, and never fail on characters the font cannot encode.

#### Scenario: Readable multi-page PDF

- **WHEN** a report has more rows than fit on a page and characters outside the font encoding
- **THEN** the PDF spans several pages with the header repeated and those characters replaced by ?

