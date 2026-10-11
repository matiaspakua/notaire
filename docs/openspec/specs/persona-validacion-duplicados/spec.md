# persona-validacion-duplicados Specification

## Purpose

Prevents two `Person` records from coexisting with the same
identification type and identification number, so budgets, managements,
and payments are never split across two unlinked records of the same
real person (CU17, CU18). Enforced in the application service (#835) and
at the database via unique index on
`people (fk_id_identification_type, identification_number)` (#799).

## Requirements

### Requirement: Database uniqueness of person identification type and number

The system SHALL enforce at the PostgreSQL schema level that no two rows
in `people` share the same `(fk_id_identification_type, identification_number)`.
Migrations MUST fail fast (or apply agreed cleanup keeping the lowest id)
when duplicate groups already exist before creating the unique index.

#### Scenario: Second insert with same type and number is rejected by the database

- **WHEN** a second `people` row is inserted with the same identification type
  and identification number as an existing row, bypassing the service-layer check
- **THEN** the database rejects the insert (unique constraint / unique index
  violation)

#### Scenario: Migration refuses to proceed when duplicate groups exist

- **WHEN** Flyway applies the uniqueness migration and the `people` table
  already contains one or more groups with the same type and number
- **THEN** the migration fails with a clear error (or completes only after
  keeping the lowest `id` per agreed cleanup) and does not leave the schema
  without the uniqueness guarantee

#### Scenario: Concurrent create race still surfaces as HTTP 409

- **WHEN** two create requests race past the service-layer duplicate check and
  one insert wins at the database
- **THEN** the losing request is mapped to HTTP 409 Conflict compatible with
  the existing #835 client path (including `existingPersonId` when resolvable)

### Requirement: Reject creating a person with a duplicate identification

The system SHALL reject creating a `Person` when another `Person` already
exists with the same `IdentificationType` and `identificationNumber`,
per CU17 and CU18. The rejection SHALL apply both in the application
service (#835) and through the database unique constraint (#799).

#### Scenario: Successful create with an unregistered identification

- **WHEN** a user creates a person with an identification type and
  `identificationNumber` that belongs to no existing person
- **THEN** the system creates the person

#### Scenario: Reject a create with an already registered identification

- **WHEN** a user attempts to create a person with the same identification
  type and `identificationNumber` as an existing person
- **THEN** the system rejects the create and responds with an error that
  identifies the existing person (`existingPersonId` when resolvable)

### Requirement: Reject editing a person toward a duplicate identification

The system SHALL reject editing a `Person` when the proposed identification
type and `identificationNumber` already belong to a different `Person`,
per CU17 and CU18.

#### Scenario: Successful edit that does not change the identification

- **WHEN** a user edits an existing person without changing the
  identification type or `identificationNumber`
- **THEN** the system saves the changes

#### Scenario: Reject an edit toward another person's identification

- **WHEN** a user edits a person, changing the identification type or
  `identificationNumber` to that of another existing person
- **THEN** the system rejects the edit and responds with an error that
  identifies the existing person with that identification
