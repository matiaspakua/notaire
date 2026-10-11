# folio-vinculacion-escritura Specification

## Purpose

Allows recording that a folio is linked to the deed that occupies it,
completing the notarial protocol described in CU87, and stops two different
deeds from occupying the same folio.

## Requirements

### Requirement: Link a folio to a deed
The system SHALL allow linking a folio to a deed when creating or editing
the folio, and SHALL mark the folio as `Utilizado` when it does so.

#### Scenario: Create a folio linked to a deed
- **WHEN** a folio is created with the `deedId` of an existing deed
- **THEN** the folio is stored linked to that deed and with
  `status = "Utilizado"`

#### Scenario: Edit a folio to link it to a deed
- **WHEN** an existing folio with no linked deed is edited, providing a
  `deedId`
- **THEN** the folio is stored linked to that deed and with
  `status = "Utilizado"`

#### Scenario: Create a folio without a link
- **WHEN** a folio is created without a `deedId`
- **THEN** the folio is stored with no linked deed, with the `status`
  provided in the request (behavior unchanged)

### Requirement: Reject linking a folio that is already in use
The system SHALL reject linking a folio to a deed when
that folio is already linked to another deed and its `status` is `Utilizado`.

#### Scenario: Folio already linked to another deed
- **WHEN** linking a folio to a deed is attempted and that folio's `status` is
  already `Utilizado` and it is linked to a different deed
- **THEN** the system rejects the request with `409 Conflict` and the folio does
  not change

#### Scenario: Link the same folio to the same deed again
- **WHEN** a folio is edited with the same `deedId` it was already
  linked to
- **THEN** the system accepts the request without an error (it is not a conflict;
  it is an idempotent update)

### Requirement: Read the deed linked to a folio
The system SHALL include the linked deed, when one exists, in the
folio read response.

#### Scenario: Read a folio with a linked deed
- **WHEN** a folio is read (`GET /api/v1/folios/{id}` or the list) that
  has a linked deed
- **THEN** the response includes that deed's data

#### Scenario: Read a folio with no linked deed
- **WHEN** a folio that has no linked deed is read
- **THEN** the response does not include deed data for that folio
