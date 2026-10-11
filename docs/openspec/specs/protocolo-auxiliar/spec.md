# protocolo-auxiliar Specification

## Purpose

Distinguishes Protocolo Auxiliar folios from Protocolo Principal folios and
allows starting an auxiliary-protocol deed with its own consecutive number
and without generating a procedure folder (CU81).

## Requirements

### Requirement: Mark a folio type as Protocolo Auxiliar
The system SHALL allow marking a folio type as belonging to Protocolo
Auxiliar through `isAuxiliary`.

#### Scenario: Mark a folio type as auxiliary
- **WHEN** a folio type is marked as belonging to Protocolo Auxiliar
  (`isAuxiliary = true`)
- **THEN** the system stores the mark and that type is available for
  Protocolo Auxiliar

#### Scenario: An unmarked folio type belongs to Protocolo Principal
- **WHEN** a folio type that was not marked as auxiliary is read
- **THEN** the system treats it as part of Protocolo Principal

### Requirement: List available Protocolo Auxiliar folios
The system SHALL list auxiliary folios that have no associated deed, via
`GET /api/v1/auxiliary-protocol/available-folios`.

#### Scenario: Available auxiliary folios exist
- **WHEN** the list of available Protocolo Auxiliar folios is requested
  and auxiliary folios without an associated deed exist
- **THEN** the system returns those folios

#### Scenario: No auxiliary folios are available
- **WHEN** the list of available Protocolo Auxiliar folios is requested
  and none exist without an associated deed
- **THEN** the system reports that no auxiliary folios are available

### Requirement: Start a Protocolo Auxiliar deed with its own numbering
The system SHALL start a Protocolo Auxiliar deed on an available auxiliary
folio (`POST /api/v1/auxiliary-protocol/deeds` with `idFolio`), assigning
the next consecutive number in the Protocolo Auxiliar sequence, independent
of Protocolo Principal numbering.

#### Scenario: Start an auxiliary deed on an available folio
- **WHEN** a Protocolo Auxiliar deed is started with an available auxiliary
  `idFolio`
- **THEN** the system creates the deed, assigns the next consecutive
  Protocolo Auxiliar number, and links it to the chosen folio

#### Scenario: Attempt to start a deed when no auxiliary folio is available
- **WHEN** starting a Protocolo Auxiliar deed is attempted and no auxiliary
  folio is available
- **THEN** the system rejects the request, reports the lack of folios, and
  creates no deed

#### Scenario: Protocolo Auxiliar numbering is independent of Protocolo Principal
- **WHEN** a new Protocolo Auxiliar deed is started and numbered deeds
  already exist in Protocolo Principal
- **THEN** the assigned consecutive number continues the Protocolo Auxiliar
  sequence and is not affected by Protocolo Principal numbering

### Requirement: Do not generate a procedure folder for Protocolo Auxiliar
The system SHALL start the Protocolo Auxiliar deed without generating a
procedure folder.

#### Scenario: Protocolo Auxiliar deed without a folder
- **WHEN** a Protocolo Auxiliar deed is started
- **THEN** the system generates no associated procedure folder
