# ui-list-pagination — delta

## Purpose

Lists and pickers backed by growing tables never load the whole table.

## MODIFIED Requirements

### Requirement: Budget pickers search the server

Forms that choose a budget SHALL list the newest budgets for an empty query, search by budget number and by client (name or document) as the user types (debounced), load the selected budget by id, and SHALL NOT request `size=1000`.

#### Scenario: Oldest budget by number

- **WHEN** the oldest budget's number is typed in the payment form's budget picker
- **THEN** the option appears, is picked by keyboard and the balance panel shows

#### Scenario: Budget by client name

- **WHEN** a new client's last name is typed in the new-management budget picker
- **THEN** the client's budget is listed and can be picked

#### Scenario: Combobox semantics

- **WHEN** the picker is opened, searched and navigated
- **THEN** aria-controls and aria-activedescendant follow, stale options are never picked, the value is loaded by id

#### Scenario: No size=1000 for budgets

- **WHEN** the source tree is scanned
- **THEN** no page or component calls usePresupuestos()
