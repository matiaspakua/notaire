# mobile-list-cards — delta

## Purpose

List pages are usable at phone widths.

## MODIFIED Requirements

### Requirement: Rows are cards on phones

Below 768px, `DataTable` SHALL render each row as a list item with the primary field as its title, the other visible columns as label/value pairs, and the row actions visible inside the viewport; at 768px and above it SHALL render the table.

#### Scenario: Phone width

- **WHEN** a list page at 390x844 with one row
- **THEN** the row is a card, its edit button is in the viewport, and the page does not scroll horizontally

#### Scenario: Desktop width

- **WHEN** the personas page at 1440px
- **THEN** the table renders and no card list exists

#### Scenario: Column mapping

- **WHEN** DataTable renders at 390px with id, name, email, a hidden column and actions
- **THEN** the title is the name, ID and Email are label/value pairs, the hidden column is absent and the actions are shown
