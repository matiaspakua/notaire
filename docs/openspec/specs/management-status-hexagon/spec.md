# management-status-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1719-management-status-hexagon. Update Purpose after archive.

## Requirements

### Requirement: The REST behaviour of management statuses is unchanged

The management-statuses endpoints MUST keep their status codes, bodies and error shapes.

#### Scenario: read search and in-use

- **WHEN** statuses are listed, searched by name, read by id and checked for use
- **THEN** the answers match the old implementation; unknown id answers empty 404; `in-use` is true when a deed management references the status

#### Scenario: a referenced status is locked

- **WHEN** a status referenced by deed managements is updated or deleted
- **THEN** the answer is 409 with the same error body as before (edit vs delete messages)

#### Scenario: stale version

- **WHEN** an unused status is updated with a version that is not the stored one
- **THEN** the answer is the contract this slice pins (explicit use-case check → standard stale 409 if that is the deliberate correction; otherwise the legacy behaviour) and any change is called out in the PR

### Requirement: The rules live in use cases over domain-typed ports

The management status catalog MUST be reachable only through domain-typed inbound ports; its rules MUST run in use cases that do not depend on Spring or a database, and the ArchUnit baseline MUST shrink for the controller.

#### Scenario: use cases without Spring

- **WHEN** the use-case tests run
- **THEN** they use an in-memory adapter and no Spring context

#### Scenario: architecture baseline shrinks

- **WHEN** the ArchUnit baseline is shrunk
- **THEN** the ManagementStatusController entries are gone and the guard passes
