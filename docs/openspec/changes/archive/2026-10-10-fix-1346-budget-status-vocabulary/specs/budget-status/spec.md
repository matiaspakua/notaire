# budget-status — delta

## Purpose

The budget status vocabulary shared by the API, the database and the UI.

## MODIFIED Requirements

### Requirement: Budget status vocabulary

The API SHALL accept only BORRADOR, PENDIENTE, APROBADO, RECHAZADO or FACTURADO (any letter case) as a budget status, SHALL store the code and SHALL answer 400 otherwise; the UI SHALL offer and translate every code.

#### Scenario: Case-insensitive write

- **WHEN** a budget is created with status `Pendiente`
- **THEN** it is stored and returned as PENDIENTE

#### Scenario: Unknown status

- **WHEN** a budget is created with status `Pending`
- **THEN** the API answers 400 naming the accepted values

#### Scenario: Migration

- **WHEN** budgets hold Spanish and English spellings in any case
- **THEN** V44 stores the codes and keeps unknown values

#### Scenario: Filter and edit

- **WHEN** a pending budget exists
- **THEN** the Pendiente filter finds it and the edit dialog shows Pendiente
