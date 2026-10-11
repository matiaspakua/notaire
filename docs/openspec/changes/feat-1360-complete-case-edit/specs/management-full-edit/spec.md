# management-full-edit — delta

## Purpose

A management is edited in full from the list, prefilled from its current case values.

## ADDED Requirements

### Requirement: Full management edit

The management edit dialog SHALL show every case field (number, budget, notary, status, procedure type, property) prefilled from GET /api/v1/managements/{id}/complete-case and SHALL save through PUT /api/v1/managements/{id}/complete-case keeping the header, notes and status; the status SHALL be read-only in the dialog; the API SHALL return the case values in the PUT's shape and 404 for an unknown management.

#### Scenario: Edit dialog is prefilled, status read-only

- **WHEN** a user clicks "Editar" on a management
- **THEN** the number, notary, budget and procedure type show the stored values and the status select is disabled

#### Scenario: Notary change persists, notes kept

- **WHEN** a user changes the notary and saves
- **THEN** the management is stored with the new notary and its notes are unchanged

#### Scenario: Substitution redirect on edit shows a toast

- **WHEN** a user changes the notary to one with an active substitution and saves
- **THEN** the management is stored with the substitute notary and a toast explains the redirect

#### Scenario: Case values read in the PUT's shape

- **WHEN** a client sends GET /api/v1/managements/{id}/complete-case for an existing management
- **THEN** the response carries number, heading, notes, budgetId, notaryId, statusManagementId, typeProcedureId and propertyId

#### Scenario: Unknown management is 404

- **WHEN** a client sends GET /api/v1/managements/{id}/complete-case for an id that does not exist
- **THEN** the API answers 404
