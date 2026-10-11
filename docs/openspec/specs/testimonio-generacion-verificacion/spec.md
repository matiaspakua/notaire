# testimonio-generacion-verificacion Specification

## Purpose

Provides the business actions to generate a testimony from a signed
deed (CU07), verify it (CU08), and issue its printed copy (RF-94).

## Requirements

### Requirement: Generate a testimony from a signed deed
The system SHALL allow generating a testimony only from a
deed in status "Firmada", per CU07 and RF-31. It SHALL reject
generation when the source deed is not signed.

#### Scenario: Successful generation from a signed deed
- **WHEN** the notary (Escribano) generates a testimony from a deed in
  status "Firmada"
- **THEN** the system creates the testimony linked to that deed and
  responds with the created testimony

#### Scenario: Rejection when generating from an unsigned deed
- **WHEN** the notary (Escribano) tries to generate a testimony from a
  deed that is not in status "Firmada"
- **THEN** the system rejects the operation without creating the testimony and
  responds with an error that the deed is not signed

### Requirement: Verify a generated testimony
The system SHALL allow verifying a testimony that was already generated, recording
whether it was marked Observado and, when it was, the reason, per CU08.

#### Scenario: Verification with no notes
- **WHEN** the notary (Escribano) verifies a generated testimony and reports
  no notes
- **THEN** the system marks the testimony as verified and not observed

#### Scenario: Verification with notes
- **WHEN** the notary (Escribano) verifies a generated testimony and reports
  an observation reason
- **THEN** the system marks the testimony as verified and Observado,
  recording the reason provided

#### Scenario: Rejection when verifying a testimony that does not exist
- **WHEN** verifying a testimony is attempted whose identifier does not exist
- **THEN** the system rejects the operation and responds with a
  resource-not-found error

### Requirement: Issue a printed copy of the verified testimony
The system SHALL allow issuing a printed copy (PDF) of a testimony
only after it has been verified, per RF-94, reusing the
existing report-generation pattern.

#### Scenario: Issue a PDF copy of a verified testimony
- **WHEN** the notary (Escribano) requests the printed copy of a testimony that
  is already verified
- **THEN** the system generates and returns the PDF of the copy

#### Scenario: Rejection when issuing a copy of an unverified testimony
- **WHEN** the notary (Escribano) requests the printed copy of a testimony that
  has not been verified yet
- **THEN** the system rejects the operation and responds with an error that
  the testimony is not verified
