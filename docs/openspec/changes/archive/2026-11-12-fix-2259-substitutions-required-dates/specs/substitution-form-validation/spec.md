<!-- Governed by CONSTITUTION.md. CU22, issue #2259. -->

## ADDED Requirements

### Requirement: Substitution dates are required in the form

The substitution dialog SHALL mark the start and end dates as required. Saving with either missing SHALL show the required message as that field's error (aria-invalid true, aria-describedby to the message), SHALL send no request, and the error of a field SHALL clear when a date is entered in it.

#### Scenario: both dates missing

- **WHEN** the dialog is saved with no dates
- **THEN** both date inputs are aria-invalid with a described error and no request is sent

#### Scenario: entering a date clears its error

- **WHEN** a date is entered after the error appeared
- **THEN** that input is no longer aria-invalid while the other keeps its error

#### Scenario: both dates present

- **WHEN** both dates are filled and the dialog is saved
- **THEN** the substitution is sent to the API
