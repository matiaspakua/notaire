# jpa-fetch-strategy Specification

## Purpose
JPA associations that grow with usage are not fetched eagerly.
## Requirements
### Requirement: Lazy budget payment list

Loading a budget SHALL NOT load its payment collection; the collection SHALL be loaded only when it is read inside a persistence context.

#### Scenario: Finding a budget

- **WHEN** a budget is found by id
- **THEN** its payment collection is not loaded

#### Scenario: Listing budgets

- **WHEN** all budgets are listed
- **THEN** no payment collection is loaded

#### Scenario: Payments read on demand

- **WHEN** the payment collection is read inside a persistence context
- **THEN** it contains every stored payment row for that budget

### Requirement: Lazy management status history

Loading a management status SHALL NOT load its history collection; the collection SHALL be loaded only when it is read inside a persistence context.

#### Scenario: Finding a status

- **WHEN** a management status is found by id
- **THEN** its history collection is not loaded

#### Scenario: Listing statuses

- **WHEN** all management statuses are listed
- **THEN** no history collection is loaded

#### Scenario: History read on demand

- **WHEN** the history collection is read inside a persistence context
- **THEN** it contains every stored history row of that status

