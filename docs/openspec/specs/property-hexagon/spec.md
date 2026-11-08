# property-hexagon Specification

## Purpose

TBD - created by archiving change refactor-1702-property-hexagon. Update Purpose after archive.

## Requirements

### Requirement: The REST behaviour of properties is unchanged

The properties endpoints MUST keep their status codes and success bodies.

#### Scenario: create read update delete

- **WHEN** a property is created, read, listed, updated and deleted
- **THEN** the answers are 201, 200, 200, 200 and 204, and the version increases on update

#### Scenario: missing cadastral designation

- **WHEN** a property is created or fully updated without a cadastral designation
- **THEN** the answer is 400 and nothing is stored

#### Scenario: unknown id

- **WHEN** an unknown property is read, updated or deleted
- **THEN** the answer is 404 with no body

### Requirement: Behaviour lives in use cases over domain-typed ports

The use cases MUST work with a pure domain Property and ports that mention no JPA type, and MUST be testable with an in-memory adapter.

#### Scenario: use cases without Spring

- **WHEN** the use-case tests run
- **THEN** they use an in-memory adapter and no Spring context

#### Scenario: dead wrappers are gone

- **WHEN** the production classpath is scanned
- **THEN** PropertyUseCase and PropertyService do not exist and the controller depends on no repository
