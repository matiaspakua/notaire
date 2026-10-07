# jpa-fetch-strategy — delta

## Purpose

JPA associations that grow with usage are not fetched eagerly.

## MODIFIED Requirements

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
