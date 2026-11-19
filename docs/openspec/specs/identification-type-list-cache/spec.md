# identification-type-list-cache Specification

## Purpose
TBD - created by archiving change fix-597-identification-type-cache. Update Purpose after archive.

## Requirements

### Requirement: Identification-type list cache

The identification-type list SHALL be served from memory on a repeated read, and a successful create, update, or delete SHALL drop that memory so the next list read sees the write.

#### Scenario: repeated list read

- **WHEN** the list is read twice with no write between the reads
- **THEN** the persistence port is asked only once

#### Scenario: a write drops the list

- **WHEN** a type is created after the list was cached
- **THEN** the next list read includes the new type and asks the port again
