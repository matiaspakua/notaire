<!-- Governed by CONSTITUTION.md. CU76, issue #2093. Evidence: vitest --coverage on the budgets page (branches 87.66); four mutation checks. -->

## ADDED Requirements

### Requirement: The budgets page rules are unit tested

The frontend unit suite MUST assert that required fields block a save, that a template that cannot be loaded warns without undoing the budget, that a procedure without a template shows the curated message, that a status outside the vocabulary stays visible but not selectable, and that the list narrows by name, number and status.

#### Scenario: a missing template does not undo the budget

- **WHEN** a budget is created with a procedure type whose template cannot be loaded
- **THEN** the budget is created, a warning is shown and no error replaces the success

#### Scenario: required fields

- **WHEN** a budget is saved without a client, a date or an amount
- **THEN** three field errors are shown and nothing is sent
