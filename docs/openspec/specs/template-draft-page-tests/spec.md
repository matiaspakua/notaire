# template-draft-page-tests Specification

## Purpose
TBD - created by archiving change test-1671-template-draft-page-tests. Update Purpose after archive.

## Requirements

### Requirement: The budget template and registration draft rules are unit tested

The frontend unit suite MUST assert that a budget template is edited and deleted through its composite key with the key fields locked on edit, and that a registration draft offers one step per status with each step gated on its required fields. The Vitest coverage floors MUST be at least 70% statements, 63% branches, 66% functions and 71% lines.

#### Scenario: a template is edited through its key

- **WHEN** a template is edited
- **THEN** the PUT targets the procedure-type and concept path and the key selects are disabled

#### Scenario: a status offers one step

- **WHEN** a draft is Generada, Presentado para inscripción or Inscripto
- **THEN** only the step that status allows is offered

#### Scenario: a step needs its fields

- **WHEN** a step dialog is open without its required fields
- **THEN** its confirm button is disabled
