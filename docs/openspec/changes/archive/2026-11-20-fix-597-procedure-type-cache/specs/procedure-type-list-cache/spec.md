<!-- Governed by CONSTITUTION.md. CU74, issue #597. This change completes the catalogs named there. -->

## ADDED Requirements

### Requirement: Procedure-type list cache

The procedure-type list SHALL be served from memory on a repeated read, and a successful create, update, delete, or workflow assignment SHALL drop that memory so the next list read sees the write.

#### Scenario: repeated list read

- **WHEN** the list is read twice with no write between the reads
- **THEN** the persistence port is asked only once

#### Scenario: a write drops the list

- **WHEN** a procedure type is created after the list was cached
- **THEN** the next list read includes the new type and asks the port again

#### Scenario: assigning a workflow drops the list

- **WHEN** a workflow is assigned after the list was cached
- **THEN** the next list read shows that assignment and asks the port again
