<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## ADDED Requirements

### Requirement: Database uniqueness of person identification type and number

The system SHALL enforce at the PostgreSQL schema level that no two rows in
`people` share the same `(fk_id_tipo_identificacion, identification_number)`,
matching the service-layer duplicate rule from #835 / CU17 / CU18. The Flyway
migration MUST fail fast with a clear message when duplicate groups already
exist (or apply an agreed cleanup that keeps the lowest `id`), then create the
unique index or constraint.

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

## MODIFIED Requirements

### Requirement: Rechazar alta de persona con documento duplicado

The system SHALL reject creating a `Person` when another `Person` already exists
with the same identification type and number, per CU17 and CU18. Rejection MUST
be enforced both in the application service (#835) and by the database unique
constraint added by this change.

#### Scenario: Alta exitosa con documento no registrado

- **WHEN** a user creates a person with a type and identification number that
  belong to no existing person
- **THEN** the system creates the person

#### Scenario: Rechazo de alta con documento ya registrado

- **WHEN** a user tries to create a person with the same type and identification
  number as an existing person
- **THEN** the system rejects the create and responds with an error that
  identifies the existing person
