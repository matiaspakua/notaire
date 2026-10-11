# gestion-bitacora Specification

## Purpose

Records every relevant status change of a management (creation, valid
transition, archiving) as a `ManagementHistory` entry, and exposes that
history for consultation, so the history CU13 promises to show is actually
populated for real managements.

## Requirements

### Requirement: Record management status changes in ManagementHistory

The system SHALL create a History entry (status, date, and optional notes)
whenever a management status is first set or changes on any write path that
persists status — including plain create, plain update, and complete-case
update — in addition to complete-case create, valid workflow transition, and
archive already covered by #833, per CU13 / RF-110 / CU02 / CU53.

#### Scenario: Creating a management records its initial status

- **WHEN** a new management is created
- **THEN** the system records a `ManagementHistory` entry with the
  management's initial status and the creation date

#### Scenario: A valid transition records the new status

- **WHEN** a management changes status through a valid transition
- **THEN** the system records a new `ManagementHistory` entry with the
  destination status and the change date

#### Scenario: Archiving records the archived status

- **WHEN** a management is archived
- **THEN** the system records a new `ManagementHistory` entry with status
  "Archivada" and the archive date

#### Scenario: Plain create with status writes initial History

- **WHEN** a client creates a management via plain POST with a management status
- **THEN** the system appends one History row for that initial status

#### Scenario: Plain create without status writes no History

- **WHEN** a client creates a management via plain POST without a management status
- **THEN** the system does not invent a History row

#### Scenario: Plain update that changes status writes History

- **WHEN** a client updates a management via plain PUT and the status id differs
  from the previous status id
- **THEN** the system appends a History row for the new status

#### Scenario: Plain update that keeps status writes no History

- **WHEN** a client updates a management via plain PUT and the status id is
  unchanged (or omitted so status stays the same)
- **THEN** the system does not append a History row for that update

#### Scenario: Complete-case update that changes status writes History

- **WHEN** a client updates a management via PUT complete-case and the status id
  differs from the previous status id
- **THEN** the system appends a History row for the new status

#### Scenario: Complete-case update that keeps status writes no History

- **WHEN** a client updates a management via PUT complete-case and the status id
  is unchanged
- **THEN** the system does not append a History row for that update

### Requirement: Read a management's history

The system SHALL allow reading the full `ManagementHistory` of a management,
ordered chronologically, per CU13, via
`GET /api/v1/managements/{id}/history`.

#### Scenario: The read returns the full history in order

- **WHEN** a user reads the history of a management that has recorded
  `ManagementHistory` entries
- **THEN** the system responds with every `ManagementHistory` entry of that
  management, ordered by date

### Requirement: Current status falls back to entity status when History is empty

The system SHALL expose the current status of a management via
`GET /api/v1/managements/{id}/current-status`. When History rows exist, the
latest by date is used. When History is empty, the system MUST synthesize a
summary from the management's current status association (management id and
as-of-now). A missing management or a null status MUST yield 404. Per CU13 /
RF-24.

#### Scenario: Current status comes from History when rows exist

- **WHEN** a client requests current status for a management that has History rows
- **THEN** the system returns 200 with the latest History summary by date

#### Scenario: Current status falls back to the entity status when History is empty

- **WHEN** a client requests current status for an existing management that has a
  status but no History rows
- **THEN** the system returns 200 with a synthesized summary from the entity
  status (management id, status id/name, as-of-now)

#### Scenario: Current status returns 404 when the management is missing

- **WHEN** a client requests current status for a management id that does not exist
- **THEN** the system returns 404

#### Scenario: Current status returns 404 when status is null and History is empty

- **WHEN** a client requests current status for an existing management with no
  History rows and no status assigned
- **THEN** the system returns 404
