<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md. -->

## Purpose

Record the data CU44 requires when a withdrawn testimony is re-entered. Source: #851; owner CU44.

## ADDED Requirements

### Requirement: Reentry stores its data

The system MUST store the cartón number, the observed-by-registry flag and the notes on the new movement created by a reentry and MUST NOT alter the previous movement.

#### Scenario: A reentry with data

- **WHEN** a testimony was withdrawn and the reentry is posted with cardNumber 77, observedByRegistry true and notes
- **THEN** the new movement holds those values and the previous movement is unchanged

#### Scenario: A reentry without a body

- **WHEN** a testimony was withdrawn and the reentry is posted with no body
- **THEN** a new movement is created with cardNumber 0, observedByRegistry false and no notes

### Requirement: Observed reentry needs notes

The system MUST reject with 400 a reentry flagged as observed by the registry whose notes are missing or blank.

#### Scenario: Observed without notes

- **WHEN** the reentry is posted with observedByRegistry true and blank notes
- **THEN** the response is 400 and no movement is created

### Requirement: The UI asks for the data

The testimony-movement screen MUST ask for the cartón number, the observed flag and the notes before re-entering a testimony.

#### Scenario: The reentry dialog

- **WHEN** the user clicks the reingresar action of a withdrawn testimony
- **THEN** a dialog asks for the three fields and posts them on confirmation
