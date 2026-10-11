# presupuesto-catalogo-items Specification

## Purpose

Lets a user add existing reusable `Item`s from the catalog (CU71) to a
`Budget`, instead of only being able to create a new `Item` from
scratch for each one.

## Requirements

### Requirement: Add catalog items to a budget
The system SHALL allow one or more existing catalog `Item`s to be
linked to a `Budget`, creating a copy of each one with its name,
amount, percentage, and notes, linked to that `Budget`, per
CU71 and RF-07. The add path is
`POST /api/v1/budgets/{id}/items-from-catalog`.

#### Scenario: Successful add of one catalog item
- **WHEN** a user selects an existing catalog item and adds it
  to a `Budget`
- **THEN** the system creates a copy of the item linked to the `Budget`,
  with the same name, amount, percentage, and notes

#### Scenario: Add of several catalog items in one operation
- **WHEN** a user selects several existing catalog items and
  adds them to a `Budget`
- **THEN** the system creates a copy of each selected item, linked
  to the `Budget`

#### Scenario: Rejection when the catalog item does not exist
- **WHEN** a user tries to add a catalog item to a `Budget`
  whose id does not exist
- **THEN** the system rejects the operation and responds with an error
  that the item does not exist
