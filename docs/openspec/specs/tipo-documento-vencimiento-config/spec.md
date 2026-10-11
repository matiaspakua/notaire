# tipo-documento-vencimiento-config Specification

## Purpose

Allows loading and editing, from the administration screen, whether a
document type expires, in how many days, and who is responsible for
delivering it, completing data that already exist in the model (CU27, CU32).

## Requirements

### Requirement: Load expiration and the responsible party when creating a document type

The system SHALL allow entering, when creating a document type, whether it
expires (`expires`), the number of validity days (`dueDays`), and who is
responsible for delivering or returning it (`deliveredBy`), according to CU27.
This change keeps that behavior and adds residual `enabled` / `returned`
alongside it (see the requirements below).

#### Scenario: Create a document type that expires

- **WHEN** an administrator creates a document type indicating that it expires,
  with a number of validity days and a responsible party
- **THEN** the system stores the document type with `expires = true`, the given
  `dueDays`, and the given `deliveredBy`

#### Scenario: Create a document type that does not expire

- **WHEN** an administrator creates a document type indicating that it does not
  expire
- **THEN** the system stores the document type with `expires = false` without
  requiring `dueDays`

### Requirement: Modify expiration and the responsible party of a document type

The system SHALL allow modifying `expires`, `dueDays`, and `deliveredBy` of
an existing document type that is not in use, according to CU32.

#### Scenario: Modify expiration and the responsible party

- **WHEN** a user modifies an existing document type that is not in use,
  changing `expires`, `dueDays`, or `deliveredBy`
- **THEN** the system stores the new values

#### Scenario: Modification blocked because the document type is in use

- **WHEN** a user attempts to modify a document type that is already in use
  (referenced by a procedure template or a submitted document)
- **THEN** the system rejects the modification with a message stating that
  a new document type must be created

### Requirement: Load enabled and returned when creating a document type

The system SHALL allow an administrator to set whether a document type is
enabled (`enabled`) and whether documents of that type are returned
(`returned`) when creating a document type, in addition to the expiration
fields already covered by this capability (CU27). On the empty create form,
defaults SHALL be `enabled = true` and `returned = false`.

#### Scenario: Create form defaults for enabled and returned

- **WHEN** an administrator opens the new document-type form
- **THEN** the system shows `enabled` checked and `returned` unchecked by
  default

#### Scenario: Create document type with enabled and returned configured

- **WHEN** an administrator creates a document type with `enabled` and
  `returned` set explicitly
- **THEN** the system persists those values and returns them on subsequent
  reads of the document type

### Requirement: Edit enabled and returned on an unused document type

The system SHALL allow modifying `enabled` and `returned` of an existing
document type that is not in use, according to CU32. The edit form SHALL
pre-fill the stored values.

#### Scenario: Edit form pre-fills enabled and returned

- **WHEN** an administrator opens an existing document type for edit
- **THEN** the system shows the stored `enabled` and `returned` values

#### Scenario: Update enabled and returned succeeds when not in use

- **WHEN** an administrator updates `enabled` and/or `returned` on a document
  type that is not in use
- **THEN** the system persists the new values
