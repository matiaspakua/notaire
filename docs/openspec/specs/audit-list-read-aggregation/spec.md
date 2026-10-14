# audit-list-read-aggregation Specification

## Purpose
TBD - created by archiving change fix-1664-aggregate-list-read-audit. Update Purpose after archive.

## Requirements

### Requirement: List reads of personal data are aggregated per user and window

A read handler annotated `@AuditPersonalDataRead(aggregated = true)` MUST write one audit row for a user's first call and MUST write no further row for that user while the configured window (default 60 seconds) has not elapsed; the first call after the window MUST write a new row. Handlers not marked aggregated (single reads by id, searches) MUST write one row per call.

#### Scenario: repeated list reads inside the window write one row

- **WHEN** one user lists people twice within the window
- **THEN** exactly one audit record is saved

#### Scenario: a list read after the window writes a new row

- **WHEN** the same user lists people again after the window has elapsed
- **THEN** a second audit record is saved

#### Scenario: another user is not suppressed

- **WHEN** a second user lists people inside the first user's window
- **THEN** that user's read is audited

#### Scenario: searches and single reads are never aggregated

- **WHEN** one user searches twice and reads one person twice inside the window
- **THEN** four audit records are saved
