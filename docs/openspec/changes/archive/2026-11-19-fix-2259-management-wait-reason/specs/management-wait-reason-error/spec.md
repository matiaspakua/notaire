<!-- Governed by CONSTITUTION.md. CU76, issue #2259. The issue stays open. -->

## ADDED Requirements

### Requirement: Management hold reason error is a field error

When a folder is put on hold without a reason, the dialog SHALL show the required message as the reason field's error (aria-invalid true and aria-describedby pointing at the message), SHALL NOT show a toast for it, and SHALL send nothing to the API. The error SHALL clear when the user types a non-blank reason, and SHALL NOT be present when the dialog is opened again.

#### Scenario: confirm without reason

- **WHEN** the hold dialog is confirmed with a blank reason
- **THEN** the reason input is aria-invalid with the described error, no toast is shown and no request is sent

#### Scenario: typing clears the error

- **WHEN** the user types a reason after the error appeared
- **THEN** the reason input is no longer aria-invalid

#### Scenario: reopening

- **WHEN** the dialog is closed and opened again
- **THEN** no reason error is shown
