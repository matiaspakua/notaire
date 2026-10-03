# bruno-zero-coverage-controllers Specification

## Purpose
Close the Bruno zero-coverage gap for the sixteen controllers listed in #953 /
`backend-api/api-test/COVERAGE.md` TODO, with asserted lifecycle requests and
updated coverage documentation. Source: #953; CU76.
## Requirements
### Requirement: Each listed controller has a Bruno folder

The Bruno suite under `backend-api/api-test/` MUST include a dedicated folder
for each of: CarpetaTramite, Copia, Cuaderno, DocumentoPresentado, Gestion,
MinutaInscripcion, MovimientoTestimonio, PlantillaCostoDocumento,
ProtocoloAuxiliar, Reporte, Rol, Testimonio, WorkflowDefinition, WorkflowNode,
WorkflowTransition, WorkflowValidation (mapped to current English controllers /
paths).

#### Scenario: Sixteen folders exist for previously uncovered controllers

- **WHEN** a contributor lists `backend-api/api-test/` after this change
- **THEN** each of the sixteen controllers has a corresponding Bruno folder
  with request files covering the operations that controller exposes

### Requirement: Requests include chai assertions

Every new Bruno request MUST include chai `test(...)` assertions for at least
HTTP status and, where applicable, identity/body fields — bare calls without
assertions are not sufficient.

#### Scenario: New requests assert status (and key body fields)

- **WHEN** a new Bruno YAML request for these controllers is executed
- **THEN** its tests block fails the run if the status (and required body
  checks) do not match expectations

### Requirement: Suite passes under Development env

`bru run . -r --env Development` from `backend-api/api-test/` MUST succeed
including the new folders (backend up on the Development base URL).

#### Scenario: Full Bruno run passes with new folders

- **WHEN** `bru run . -r --env Development` is executed against a running API
- **THEN** the run exits successfully and includes the new folders

### Requirement: Suites are idempotent

New folders MUST not leak durable rows across a second consecutive full suite
run (fixtures + teardown / unique keys), consistent with #1035.

#### Scenario: Second consecutive Bruno run stays green

- **WHEN** the full Bruno suite is run twice in a row against the same database
- **THEN** both runs succeed without failures caused by leftover data from the
  first run

### Requirement: Coverage documentation is updated

`COVERAGE.md`, `TEST-PLAN.md` (§7 Bruno/API contract notes), and
`CU-API-MATRIX.csv` MUST reflect the new coverage and MUST NOT still list those
sixteen controllers as zero-coverage TODOs.

#### Scenario: COVERAGE TODO no longer lists the sixteen

- **WHEN** `backend-api/api-test/COVERAGE.md` is read after this change
- **THEN** the sixteen controllers appear under covered resources (or an
  honest partial note for specific report endpoints) and are not listed as
  zero-coverage TODO items

#### Scenario: Matrix and TEST-PLAN reflect Bruno status

- **WHEN** `CU-API-MATRIX.csv` and `TEST-PLAN.md` §7 are inspected
- **THEN** Bruno columns/notes for the newly covered endpoints match the suite

