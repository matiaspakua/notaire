# minuta-inscripcion Specification

## Purpose

Generates the registration draft (Minuta de Inscripción) for deeds on
properties and tracks its registry circuit (submission, observations, final
registration) before the property registry (CU82).

## Requirements

### Requirement: Record a property's registry data

The system SHALL allow recording a property's `registrationNumber`,
`volumeFolioLandRecord`, and `boundaries`.

#### Scenario: Load a property's registry data

- **WHEN** a property's `registrationNumber`, `volumeFolioLandRecord`, and
  `boundaries` are completed
- **THEN** the system stores those data together with the existing
  `cadastralDesignation` and fiscal appraisal

### Requirement: Generate the registration draft

The system SHALL allow generating the registration draft for a deed on a
property with an approved procedure, provided the required cadastral and
registry data are complete. A successful generate via
`POST /api/v1/registration-drafts` SHALL return HTTP `201 Created` with a
`Location` header pointing at `/api/v1/registration-drafts/{id}`.

#### Scenario: Generate a draft with complete data

- **WHEN** generating the registration draft is requested for a deed on a
  property whose procedure is approved and whose cadastral and registry
  data are complete
- **THEN** the system generates the draft with an identifier number, in
  status "Generada", responds `201 Created`, and includes the draft's
  `Location`

#### Scenario: Attempt to generate a draft with incomplete data

- **WHEN** generating the registration draft is requested for a property
  that is missing required cadastral or registry data
- **THEN** the system rejects the request, reports the missing fields, and
  does not generate the draft

#### Scenario: Print the draft on the standard form

- **WHEN** the document of an already generated draft is requested
- **THEN** the system returns the report as a PDF. The download name stays
  `minuta_inscripcion_{idRegistrationDraft}.pdf`

### Requirement: Submit the draft to the registry

The system SHALL allow recording submission of the draft to the property
registry.

#### Scenario: Record the submission

- **WHEN** submission of a generated draft is recorded, giving the date and
  the registry entry number (`registryEntryNumber`)
- **THEN** the system stores those data and changes the draft status to
  "Presentado para inscripción"

### Requirement: Record registry observations

The system SHALL allow recording observations made by the registry on a
submitted draft.

#### Scenario: Record an observation

- **WHEN** the registry makes observations on a submitted draft
- **THEN** the system stores the observations and the correction date, and
  changes the draft status to "Observado"

### Requirement: Record the final registration

The system SHALL allow recording the final registration of a submitted
draft.

#### Scenario: Record the final registration

- **WHEN** return of the registered testimony is recorded, giving the
  reception date and the final registration number
- **THEN** the system stores those data and changes the draft status to
  "Inscripto"
