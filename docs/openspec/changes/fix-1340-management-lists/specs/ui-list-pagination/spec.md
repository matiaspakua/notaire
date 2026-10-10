# ui-list-pagination — delta

## Purpose

Lists and pickers backed by growing tables never load the whole table.

## MODIFIED Requirements

### Requirement: Management lists and pickers are server-paged

The external-entity documents and documentation re-entry screens SHALL list managements one server page at a time, newest first, with page and size in the URL; the presented-documents form SHALL choose its management with a server-search picker; no screen SHALL request `GET /gestiones?size=1000`.

#### Scenario: Newest case on page one

- **WHEN** a user creates a management and opens /dashboard/reingreso-documentacion or /dashboard/documentos-entidades-externas
- **THEN** the new management is listed on the first page with the pagination footer

#### Scenario: Page past the end

- **WHEN** the URL asks for a page past the last one
- **THEN** the list moves to the last page

#### Scenario: Pick any management

- **WHEN** a user types a case number, a client name or a client document number in the document form's management picker
- **THEN** the matching managements are offered, whatever their position in the table
