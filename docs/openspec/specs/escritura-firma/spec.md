# escritura-firma Specification

## Purpose

Provides the "Firmar escritura" business action (CU06): a controlled state
transition that replaces the current unrestricted generic PUT on `Deed`.

## Requirements

### Requirement: Sign a deed only when it is Sin Firmar and has a folio
The system SHALL allow signing a deed only when it is in status
"Sin Firmar" and has at least one folio assigned, per CU06 and RF-27. The
action SHALL reject the signature in every other case, instead of accepting
an arbitrary status change through the generic CRUD endpoint.

#### Scenario: Successful signature of a deed that is ready
- **WHEN** the notary (Escribano) invokes the signature action on a deed in
  status "Sin Firmar" that has one or more folios assigned
- **THEN** the system changes the deed status to "Firmada" and
  responds with the updated deed

#### Scenario: Rejection because the deed is already signed
- **WHEN** the notary (Escribano) invokes the signature action on a deed that
  is already in status "Firmada" or later
- **THEN** the system rejects the operation without changing the status and
  responds with an error that reports the current status

#### Scenario: Rejection because no folio is assigned
- **WHEN** the notary (Escribano) invokes the signature action on a deed in
  status "Sin Firmar" that has no folio assigned
- **THEN** the system rejects the operation without changing the status and
  responds with an error that reports the missing folio
