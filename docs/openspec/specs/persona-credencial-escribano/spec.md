# persona-credencial-escribano Specification

## Purpose

Gives the people screen a dedicated place to register or modify
a person's notary credential (`notaryRegistrationNumber`), which
already exists on the data model but cannot be managed from the UI (CU48,
CU51).

## Requirements

### Requirement: Register the notary number from the people screen
The system SHALL allow entering the notary registration number of an
existing person from the people screen, per CU48.

#### Scenario: Register a notary number
- **WHEN** a user looks up an existing person with no notary registration
  number and assigns one
- **THEN** the system stores the notary registration number on that
  person in `notaryRegistrationNumber`

### Requirement: Modify the notary number from the people screen
The system SHALL allow modifying the notary registration number of
a person who already has one, from the people screen, per CU51.

#### Scenario: Modify a notary number
- **WHEN** a user looks up a person who already has a notary registration
  number and changes it
- **THEN** the system stores the new notary registration number on that
  person in `notaryRegistrationNumber`
