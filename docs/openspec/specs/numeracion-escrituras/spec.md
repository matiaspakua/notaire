# numeracion-escrituras Specification

## Purpose

Controls that the deed number assigned when preparing or signing a
deed is unique and consecutive within the corresponding protocol, year, and
notary (Escribano), with no unjustified gaps and no duplicates (CU86).

## Requirements

### Requirement: Validate the sequence when saving a deed
The system SHALL calculate the next expected consecutive number within
the protocol (principal or auxiliary), year, and notary (Escribano) of the deed, and
compare the proposed number with that sequence before saving.

#### Scenario: The number matches the expected sequence
- **WHEN** a deed is saved whose number matches the expected sequence
  for its protocol, year, and notary (Escribano)
- **THEN** the system saves the deed as usual

### Requirement: Reject duplicate numbers
The system SHALL reject saving a deed whose number was already
used by another deed of the same protocol, year, and notary (Escribano).

#### Scenario: Number already used
- **WHEN** saving a deed is attempted with a number already used by another
  deed of the same protocol, year, and notary (Escribano)
- **THEN** the system rejects the save and reports the duplicate number

### Requirement: Require a justification for a numbering gap
The system SHALL require a recorded justification when the proposed
number leaves a gap against the expected sequence, and SHALL store
that justification with the deed.

#### Scenario: Gap without a justification
- **WHEN** saving a deed is attempted whose number leaves a gap
  against the expected sequence, without a justification
- **THEN** the system rejects the save and asks for the justification of the
  gap

#### Scenario: Gap with a justification
- **WHEN** a deed is saved whose number leaves a gap against the
  expected sequence, with a justification (for example,
  a voided deed or "no pasó")
- **THEN** the system saves the deed together with the recorded
  justification

### Requirement: Protocolo Auxiliar numbering is independent of Protocolo Principal
The system SHALL check the sequence of Protocolo Auxiliar
independently from that of Protocolo Principal.

#### Scenario: A Protocolo Auxiliar deed does not affect Protocolo Principal numbering
- **WHEN** a deed is saved on a Protocolo Auxiliar folio
- **THEN** the system calculates and validates its sequence only against
  other Protocolo Auxiliar deeds of the same year and notary (Escribano), without
  considering the Protocolo Principal numbering
