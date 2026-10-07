<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md. -->

## Purpose

Show what a gestión has produced in the post-signing workflow. Source: #774; owner CU07.

## ADDED Requirements

### Requirement: Escrituras and testimonios of the gestión

The system MUST list, for a gestión, the distinct escrituras of its trámites and, for each, its testimonios with the verified flag, the flagged flag, the state of the latest movement and the number of copias.

#### Scenario: A gestión with a deed, a testimony, a movement and a copy

- **WHEN** a trámite of the gestión has a deed whose testimony was entered, registered and has one copy
- **THEN** the summary lists the deed, the testimony with state INSCRIPTO and one copy

#### Scenario: A testimony without movements

- **WHEN** the deed has a verified testimony and no movement
- **THEN** its state is SIN_INGRESAR

#### Scenario: A withdrawn testimony

- **WHEN** the latest movement has an exit date
- **THEN** its state is RETIRADO

### Requirement: A gestión without deeds

The system MUST return an empty escrituras list, not an error, for a gestión whose trámites have no deed.

#### Scenario: No deed yet

- **WHEN** no trámite of the gestión has a deed
- **THEN** the escrituras list is empty

### Requirement: Unknown gestión

The system MUST answer 404 for a gestión that does not exist.

#### Scenario: Unknown id

- **WHEN** the summary of gestión 999999 is requested
- **THEN** the response is 404

### Requirement: The gestiones screen shows the case summary

The gestiones screen MUST offer a case summary dialog showing escrituras, testimonios with their state, copias and pagos.

#### Scenario: Open the summary

- **WHEN** the user opens the case summary of a gestión that has a deed and a testimony
- **THEN** the dialog lists the deed, the testimony state and the payment totals
