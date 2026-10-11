# frontend-i18n-page-coverage — delta

## Purpose

Spanish UI copy stays Spanish after code renames.

## MODIFIED Requirements

### Requirement: Spanish catalog

Every value in frontend/messages/es.json SHALL be Spanish copy; English entity names introduced by code renames SHALL NOT appear in es.json values.

#### Scenario: Person toast

- **WHEN** a Spanish user updates a person
- **THEN** the toast reads "Persona actualizada"

#### Scenario: Guard

- **WHEN** a code rename rewrites an es.json value to an English entity name
- **THEN** the unit suite fails naming the key

#### Scenario: Create a management

- **WHEN** a user creates a management from the dashboard
- **THEN** the request carries typeProcedureId and the management is created

#### Scenario: Users table

- **WHEN** an admin opens the users page in Spanish
- **THEN** the role column header is "Rol"
