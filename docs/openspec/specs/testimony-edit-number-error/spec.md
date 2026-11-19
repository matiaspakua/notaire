# testimony-edit-number-error Specification

## Purpose
TBD - created by archiving change fix-2259-testimony-number-error. Update Purpose after archive.

## Requirements

### Requirement: Testimony edit number error is a field error

When a testimony edit is saved with a number that is not a positive integer, the dialog SHALL show the required message as the number field's error, SHALL NOT show a toast for it, and SHALL send nothing to the API. The error SHALL clear when the user types a valid number.

#### Scenario: save without a valid number

- **WHEN** the edit dialog is saved with a blank, zero, negative, decimal, or non-numeric number
- **THEN** the number input is aria-invalid with the described error, no toast is shown and no request is sent

#### Scenario: typing clears the error

- **WHEN** the user types a positive integer after the error appeared
- **THEN** the number input is no longer aria-invalid
