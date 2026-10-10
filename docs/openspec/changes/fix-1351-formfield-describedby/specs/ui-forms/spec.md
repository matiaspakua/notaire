# ui-forms — delta

## Purpose

Form field accessibility wiring.

## MODIFIED Requirements

### Requirement: FormField describes its control

A FormField with an error SHALL mark its control aria-invalid and describe it with the error, which SHALL be announced (role=alert) without emoji; helper text SHALL describe the control when there is no error; a required field SHALL expose aria-required.

#### Scenario: Error

- **WHEN** a FormField has an error
- **THEN** the control is aria-invalid, described by the error, and the error is an alert

#### Scenario: Helper

- **WHEN** a FormField has helper text and no error
- **THEN** the control is described by the helper and keeps its own description ids

#### Scenario: Backend field error

- **WHEN** the API rejects a field
- **THEN** the dialog shows it as an alert that describes the input
