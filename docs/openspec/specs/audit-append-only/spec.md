# audit-append-only Specification

## Purpose
TBD - created by archiving change feat-1636-audit-append-only. Update Purpose after archive.

## Requirements

### Requirement: Audit rows are append-only in the database

The database MUST reject UPDATE, DELETE and TRUNCATE on `audit_records` for every role that does not first disable the `audit_records_append_only` triggers as table owner, while INSERT and SELECT keep working. The migration MUST also revoke UPDATE, DELETE and TRUNCATE on the table from PUBLIC and from the migrating role.

#### Scenario: update is rejected

- **WHEN** an UPDATE targets a row of `audit_records`
- **THEN** the statement fails with an append-only error

#### Scenario: delete is rejected

- **WHEN** a DELETE targets a row of `audit_records`
- **THEN** the statement fails with an append-only error

#### Scenario: truncate is rejected

- **WHEN** `TRUNCATE audit_records` runs
- **THEN** the statement fails with an append-only error

#### Scenario: insert and select still work

- **WHEN** a row is inserted and read back
- **THEN** both succeed
