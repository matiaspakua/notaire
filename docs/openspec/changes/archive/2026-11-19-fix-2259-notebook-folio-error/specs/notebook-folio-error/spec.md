<!-- Governed by CONSTITUTION.md. CU76, issue #2259. The issue stays open. -->

## ADDED Requirements

### Requirement: Notebook folio error is a field error

When a notebook is created from a selected folio that has no notary, the dialog SHALL show the message as the folio field's error, the checkbox SHALL be aria-invalid and described by that message, SHALL NOT show a toast for it, and SHALL send nothing. The error SHALL clear when the selection changes.

#### Scenario: selected folio has no notary

- **WHEN** create is pressed after selecting a folio with no notary
- **THEN** the checkbox is aria-invalid with the described error, no toast is shown and no request is sent

#### Scenario: changing the selection clears the error

- **WHEN** the user toggles the folio after the error appeared
- **THEN** the checkbox is no longer aria-invalid
