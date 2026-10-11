# cuadernos-de-folios Specification

## Purpose

Allows grouping notarial folios into consecutive notebooks of ten folios,
as the protocol rules require, and issuing the official cover of each
notebook (CU80).

## Requirements

### Requirement: Generate a notebook from a folio range

The system SHALL allow generating a notebook by grouping exactly ten
consecutive folios of the same notarial register. The create path is
`POST /api/v1/notebooks` with `idsFolio` and `idNotary`.

#### Scenario: Create a notebook with a valid range

- **WHEN** generating a notebook is requested with ten available,
  consecutive folios of the same notarial register
- **THEN** the system creates the notebook, assigns the next available
  consecutive number for that register and year, and groups the ten folios

#### Scenario: Folio count is not a multiple of ten

- **WHEN** generating a notebook is requested with a folio count that is
  not an exact multiple of ten
- **THEN** the system rejects the request and creates no notebook

#### Scenario: Discontinuous folio range

- **WHEN** generating a notebook is requested with missing folios or
  non-consecutive numbering
- **THEN** the system rejects the request, reports the discontinuity, and
  creates no notebook

#### Scenario: Folio already assigned to another notebook

- **WHEN** generating a notebook is requested including a folio already
  assigned to another notebook
- **THEN** the system rejects the request and creates no notebook

#### Scenario: Batch that includes a damaged folio with a justification

- **WHEN** generating a notebook is requested whose range includes a folio
  in status "Errose" or "no pasó", together with a justification for that
  folio
- **THEN** the system creates the notebook including the affected folio,
  with the justification recorded as the notebook's notes

### Requirement: Mark the folios as assigned to a notebook

The system SHALL update the status of each grouped folio to
"Asignado a cuaderno" when the notebook that contains it is generated.

#### Scenario: Folio status after the notebook is generated

- **WHEN** a notebook is generated successfully
- **THEN** each of its ten folios has status "Asignado a cuaderno"

### Requirement: Number notebooks consecutively by register and year

The system SHALL assign each notebook a consecutive number within its
notarial register and year, with no duplicates.

#### Scenario: First notebook of the year for a register

- **WHEN** the first notebook of a notarial register is generated in a
  given year
- **THEN** the notebook receives number 1

#### Scenario: Numbering conflict

- **WHEN** a new notebook is generated for a register and year that already
  have numbered notebooks, and the expected number is already taken
- **THEN** the system recalculates and assigns the next available
  consecutive number, without requiring manual intervention

### Requirement: Issue the official cover of a notebook

The system SHALL generate the official cover of a notebook, stating the
year, the notary (Escribano) register, the notebook number, the folio
range, and the detail of the deeds or procedures granted on those folios.
The path is `GET /api/v1/notebooks/{id}/cover`. The download name stays
`notebook_{id}_cover.pdf`.

#### Scenario: Issue the cover of an existing notebook

- **WHEN** the cover of an already generated notebook is requested
- **THEN** the system delivers a document with the year, the notary
  (Escribano) register, the notebook number, the folio range, and the
  detail of deeds and procedures granted on the folios that compose it

#### Scenario: Issue the cover of a notebook that does not exist

- **WHEN** the cover of a notebook that does not exist is requested
- **THEN** the system rejects the request, stating that the notebook was
  not found
