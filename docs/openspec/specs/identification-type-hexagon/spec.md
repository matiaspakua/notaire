# identification-type-hexagon Specification

## Purpose

TBD - created by archiving change refactor-1700-identification-types-hexagon. Update Purpose after archive.

## Requirements

### Requirement: The REST behaviour of the catalog is unchanged

The identification-types endpoints MUST keep their status codes, success bodies and the 404 and 409 responses.

#### Scenario: not found answers are empty 404

- **WHEN** an unknown id is read, updated or deleted
- **THEN** the answer is 404 with no body

#### Scenario: conflicts keep their reason

- **WHEN** a name already exists in another letter case, or a type used by people is deleted
- **THEN** the answer is 409 with the same error body and message as before

### Requirement: The rules live in use cases

The use cases MUST enforce the unique name (ignoring case and surrounding spaces) and refuse to delete a type used by people, using only ports.

#### Scenario: unique name

- **WHEN** a type is created or renamed to a name another type has, ignoring case
- **THEN** a name-taken domain exception is raised and nothing is saved

#### Scenario: in use

- **WHEN** a type used by people is deleted
- **THEN** an in-use domain exception is raised and the type remains

### Requirement: The controller reaches the catalog through ports only

The controller MUST NOT depend on a repository, the JPA entity or the transaction annotation.

#### Scenario: architecture baseline shrinks

- **WHEN** the ArchUnit baseline is shrunk
- **THEN** the three controller entries of this context are gone and the guard passes
