<!-- Governed by CONSTITUTION.md. CU76, issue #1703. -->

## ADDED Requirements

### Requirement: The REST behaviour of folio types is unchanged

The folio-types endpoints MUST keep their status codes, bodies and error shapes.

#### Scenario: read search and in-use

- **WHEN** types are listed, searched by name, read by id and checked for use
- **THEN** the answers and bodies match the old implementation, and an unknown id answers an empty 404

#### Scenario: a type used by folios is locked

- **WHEN** a type used by folios is updated or deleted
- **THEN** the answer is 409 with the same error body as before

#### Scenario: stale version

- **WHEN** an unused type is updated with a version that is not the stored one
- **THEN** the answer is 409 with the stale-version message

### Requirement: The rules live in use cases over domain-typed ports

The use cases MUST enforce the lock-when-in-use and delete guard using only ports and MUST be testable with an in-memory adapter.

#### Scenario: use cases without Spring

- **WHEN** the use-case tests run
- **THEN** they use an in-memory adapter and no Spring context

#### Scenario: architecture baseline shrinks

- **WHEN** the ArchUnit baseline is shrunk
- **THEN** the FolioTypeController entries are gone and the guard passes
