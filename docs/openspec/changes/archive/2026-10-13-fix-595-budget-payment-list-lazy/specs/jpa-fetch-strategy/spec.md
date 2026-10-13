<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

JPA associations that grow with usage are not fetched eagerly.

## ADDED Requirements

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
