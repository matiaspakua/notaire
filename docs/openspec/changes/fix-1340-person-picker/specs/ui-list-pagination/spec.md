# ui-list-pagination — delta

## Purpose

Lists and pickers backed by growing tables never load the whole table.

## MODIFIED Requirements

### Requirement: Person pickers search the server

Forms that choose a person SHALL search `GET /api/v1/people/search` as the user types (debounced), list the newest people for an empty query, load the selected person by id, and SHALL NOT request `size=1000`.

#### Scenario: New person found by name

- **WHEN** a person created after the first 1000 is typed in the budget client picker
- **THEN** the option appears, is picked with ArrowDown and Enter, and is saved with the budget

#### Scenario: Client filter

- **WHEN** a new client's last name is typed in the management client filter
- **THEN** picking it filters managements by that client

#### Scenario: Combobox semantics

- **WHEN** the picker is opened, navigated and closed
- **THEN** aria-expanded, aria-controls and aria-activedescendant follow, Escape does not bubble, the edit value is loaded by id

#### Scenario: No size=1000 anywhere

- **WHEN** the source tree is scanned
- **THEN** no page or component calls usePersonas()
