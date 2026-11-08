<!-- Governed by CONSTITUTION.md. Document types CU27/CU32/CU38/CU65, issue #1705, epic #1693. -->

## ADDED Requirements

### Requirement: The REST behaviour of document types is unchanged

The document-types endpoints MUST keep their status codes, bodies and error shapes.

#### Scenario: read search and in-use

- **WHEN** types are listed, searched by name, read by id and checked for use
- **THEN** the answers and bodies match the old implementation, an unknown id answers an empty 404, and `in-use` is true when either a procedure template or a submitted document references the type

#### Scenario: a type in use is locked

- **WHEN** a type used by procedure templates or submitted documents is updated or deleted
- **THEN** the answer is 409 with the same error body as before (edit vs delete messages differ)

#### Scenario: stale version

- **WHEN** an unused type is updated with a version that is not the stored one
- **THEN** the answer is 409 with the stale-version message

#### Scenario: create defaults

- **WHEN** a create omits `deliveredBy`, `enabled`, or `returned`
- **THEN** the defaults match the old controller (`""`, `true`, `false`) and an explicit `enabled=false` is not overwritten

### Requirement: The rules live in use cases over domain-typed ports

The use cases MUST enforce the lock-when-in-use and delete guard using only ports and MUST be testable with an in-memory adapter.

#### Scenario: use cases without Spring

- **WHEN** the use-case tests run
- **THEN** they use an in-memory adapter and no Spring context

#### Scenario: architecture baseline shrinks

- **WHEN** the ArchUnit baseline is shrunk
- **THEN** the DocumentTypeController entries are gone and the guard passes
