<!-- Governed by CONSTITUTION.md. CU74, issue #597. The issue stays open. -->

## ADDED Requirements

### Requirement: Role list cache

The role list SHALL be served from memory on a repeated read, and a successful create, update, or delete SHALL drop that memory so the next list read sees the write.

#### Scenario: repeated list read

- **WHEN** the list is read twice with no write between the reads
- **THEN** the persistence port is asked only once

#### Scenario: a write drops the list

- **WHEN** a role is created after the list was cached
- **THEN** the next list read includes the new role and asks the port again
