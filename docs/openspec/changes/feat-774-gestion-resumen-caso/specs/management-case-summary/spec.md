<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md. -->

## Purpose

Show what a management has produced in the post-signing workflow. Source: #774; owner CU07.

## ADDED Requirements

### Requirement: Deeds and testimonies of the management

The system MUST list, for a management, the distinct deeds of its procedures and, for each, its testimonies with the verified flag, the flagged flag, the state of the latest movement and the number of copies.

#### Scenario: A management with a deed, a testimony, a movement and a copy

- **WHEN** a procedure of the management has a deed whose testimony was entered, registered and has one copy
- **THEN** the summary lists the deed, the testimony with state INSCRIPTO and one copy

#### Scenario: A testimony without movements

- **WHEN** the deed has a verified testimony and no movement
- **THEN** its state is SIN_INGRESAR

#### Scenario: A withdrawn testimony

- **WHEN** the latest movement has an exit date
- **THEN** its state is RETIRADO

### Requirement: A management without deeds

The system MUST return an empty deeds list, not an error, for a management whose procedures have no deed.

#### Scenario: No deed yet

- **WHEN** no procedure of the management has a deed
- **THEN** the deeds list is empty

### Requirement: Unknown management

The system MUST answer 404 for a management that does not exist.

#### Scenario: Unknown id

- **WHEN** the summary of management 999999 is requested
- **THEN** the response is 404

### Requirement: The managements screen shows the case summary

The managements screen MUST offer a case summary dialog showing deeds, testimonies with their state, copies and payments.

#### Scenario: Open the summary

- **WHEN** the user opens the case summary of a management that has a deed and a testimony
- **THEN** the dialog lists the deed, the testimony state and the payment totals
