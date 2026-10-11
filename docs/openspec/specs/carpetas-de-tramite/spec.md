# carpetas-de-tramite Specification

## Purpose

Generates and manages the lifecycle (`Activa`, `Espera`, `Archivada`) of
the folder that groups the documentation of each procedure inside a
management (CU85).

## Requirements

### Requirement: Generate a procedure folder automatically
The system SHALL automatically generate a procedure folder with a unique
number, in status "Activa", when a procedure is started inside a
management.

#### Scenario: A management with a single procedure
- **WHEN** a management is started with a single procedure
- **THEN** the system generates a folder with a unique number, in status
  "Activa", linked to the management and the procedure

#### Scenario: A management that groups more than one procedure
- **WHEN** a management is started that groups more than one procedure
- **THEN** the system generates one folder per procedure, each with its
  own unique number, all linked to the same management

### Requirement: Read the status of a procedure folder
The system SHALL allow reading, at any time, the number, current status,
management, and associated procedure of a procedure folder.

#### Scenario: Read an existing folder
- **WHEN** the folder of a given procedure is requested
- **THEN** the system shows the folder number, current status, management,
  and associated procedure

#### Scenario: Read a folder that does not exist
- **WHEN** the folder of a procedure that has no registered folder is
  requested
- **THEN** the system reports that it was not found

### Requirement: Put a folder on hold
The system SHALL allow putting a procedure folder in status "Espera",
recording the reason.

#### Scenario: Folder put on hold with a reason
- **WHEN** an active folder is put on hold and a reason is given
- **THEN** the system changes the folder status to "Espera" and records
  the reason

#### Scenario: Attempt to put a folder on hold without a reason
- **WHEN** a folder is put on hold without a reason
- **THEN** the system rejects the request and does not change the folder
  status

### Requirement: Archive folders when the management is archived
The system SHALL change each procedure folder associated with a management
to "Archivada" when that management is archived (CU16), alerting and
requiring explicit confirmation when a folder is still "Espera" with an
unresolved reason. The archive request keeps the query flag `confirmado`.

#### Scenario: Archive a management whose folders are all active
- **WHEN** a management whose procedure folders are "Activa" is archived
- **THEN** the system changes all those folders to "Archivada"

#### Scenario: Archive a management with an unresolved folder on hold
- **WHEN** archiving is attempted for a management that has a folder in
  status "Espera" whose reason is still unresolved, and `confirmado` is
  false
- **THEN** the system alerts and requires explicit confirmation before
  archiving

#### Scenario: Explicit confirmation of archive with a folder on hold
- **WHEN** archiving a management with an unresolved folder in status
  "Espera" is explicitly confirmed (`confirmado`)
- **THEN** the system archives the management and changes that folder to
  "Archivada"
