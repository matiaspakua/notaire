# presupuesto-plantilla Specification

## Purpose

Loads the `Concept`s of a `ProcedureType`'s `BudgetTemplate` as
`Item`s of a real `Budget`, so quoting the same procedure type
starts from a common, previously defined price list (CU39).

## Requirements

### Requirement: Load budget items from the procedure type template
The system SHALL allow a `Budget` to be loaded with new `Item`s
from the `Concept`s of the `BudgetTemplate` linked to the chosen
`ProcedureType`, copying name, amount, and percentage at load time,
per CU39 and RF-04/RF-64 through RF-67.

#### Scenario: Successful load from an existing template
- **WHEN** a user loads the template items into a `Budget` for
  a procedure type that has a `BudgetTemplate` with one or more
  concepts
- **THEN** the system creates one `Budget` `Item` for each concept in
  the template, with the same name, amount, and percentage

#### Scenario: Loaded items are not recalculated if the template changes later
- **WHEN** the `BudgetTemplate` of a procedure type is modified
  after its items were loaded into an existing `Budget`
- **THEN** the items already loaded in that `Budget` keep the values
  they were created with, and are not updated automatically

#### Scenario: Rejection when the procedure type has no template
- **WHEN** a user tries to load template items into a
  `Budget` for a procedure type that has no
  `BudgetTemplate`
- **THEN** the system rejects the operation and responds with an error
  that the procedure type has no template configured
