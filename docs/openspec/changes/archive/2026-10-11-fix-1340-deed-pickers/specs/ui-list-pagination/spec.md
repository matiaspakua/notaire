# ui-list-pagination — delta

## Purpose

Lists and pickers backed by growing tables never load the whole table.

## MODIFIED Requirements

### Requirement: Deed pickers search the server

The testimony, registration-draft and folio forms SHALL choose their deed with a server-search picker that lists the newest signed deeds and finds any signed deed by number; the folio form SHALL NOT offer a deed linked to another folio; no source SHALL request a whole table with size=1000.

#### Scenario: Newest signed deeds on open

- **WHEN** a user opens the deed picker without typing
- **THEN** the newest signed deeds are listed and unsigned deeds are not

#### Scenario: Any signed deed by number

- **WHEN** a user types the number of a signed deed, whatever its position in the table
- **THEN** that deed is offered and can be chosen

#### Scenario: Deed already linked to a folio

- **WHEN** a user searches, in the folio form, a deed linked to another folio
- **THEN** the picker reports no results

#### Scenario: No whole-table loads

- **WHEN** the frontend sources are scanned
- **THEN** none defines or calls a size=1000 helper
