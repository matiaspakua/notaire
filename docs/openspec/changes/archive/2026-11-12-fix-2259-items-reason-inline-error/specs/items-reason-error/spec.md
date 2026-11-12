<!-- Governed by CONSTITUTION.md. CU76, issue #2259. -->

## ADDED Requirements

### Requirement: Items reason error is a field error

When an item of type discount or surcharge is saved without a reason, the dialog SHALL show the required message as the reason field's error (aria-invalid true and aria-describedby pointing at the message), SHALL NOT show a toast for it, and SHALL send nothing to the API. The error SHALL clear when the user types a reason, and SHALL NOT be present when the dialog is opened again.

#### Scenario: save without reason

- **WHEN** a discount item is saved with a blank reason
- **THEN** the reason input is aria-invalid with the described error, no toast is shown and no request is sent

#### Scenario: typing clears the error

- **WHEN** the user types a reason after the error appeared
- **THEN** the reason input is no longer aria-invalid

#### Scenario: reopening

- **WHEN** the dialog is closed and opened again
- **THEN** no reason error is shown
