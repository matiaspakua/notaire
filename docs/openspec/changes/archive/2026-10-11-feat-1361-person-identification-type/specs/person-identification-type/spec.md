# person-identification-type — delta

## Purpose

Persons carry an identification type chosen in the form, DNI by default.

## ADDED Requirements

### Requirement: Identification type on persons

The person form SHALL require an identification type from the catalog and SHALL preselect DNI for a new person; the API SHALL store DNI when a create request has no type and SHALL reject an identification type id that is not in the catalog with 400; the person list SHALL show the type name before the number.

#### Scenario: New person starts on DNI

- **WHEN** a user opens "Nueva persona"
- **THEN** the "Tipo de identificación" select, placed before the number, shows DNI

#### Scenario: Create with CUIT is listed as CUIT

- **WHEN** a user creates a person with type CUIT and number 30712345678
- **THEN** the person is stored with the CUIT type and the list shows "CUIT 30712345678"

#### Scenario: Omitted type defaults to DNI

- **WHEN** a client sends POST /api/v1/people without identificationTypeId
- **THEN** the person is created with the DNI type

#### Scenario: Unknown type id is rejected

- **WHEN** a client sends POST or PUT /api/v1/people with an identificationTypeId that is not in the catalog
- **THEN** the API answers 400 and the message names identificationTypeId

#### Scenario: Untyped person must pick a type on save

- **WHEN** a user edits a person stored without a type and saves without choosing one
- **THEN** the form shows that the type is required and sends nothing
