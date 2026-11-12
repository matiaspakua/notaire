<!-- Governed by CONSTITUTION.md. CU-78, issue #2259. -->

## ADDED Requirements

### Requirement: MFA code errors are field errors

The login second-factor step SHALL report a wrong code, a malformed code and a lockout as the code field's error (aria-invalid true, aria-describedby pointing at a role=alert message) and SHALL NOT use a toast for them. The error SHALL clear when the code is edited or the input mode is switched. A connection failure SHALL remain a toast. In the enrolment confirm step the code input SHALL be aria-invalid and described by the existing error message when a confirmation error is shown.

#### Scenario: wrong code at login

- **WHEN** the second-factor code is wrong
- **THEN** the code input is aria-invalid with a described alert, the field is cleared and no toast is shown

#### Scenario: lockout at login

- **WHEN** the API answers 429 with a message
- **THEN** that message is the code field's error

#### Scenario: editing clears the error

- **WHEN** the user edits the code or switches to the recovery code
- **THEN** the code input is no longer aria-invalid

#### Scenario: connection failure

- **WHEN** the request fails without an HTTP status
- **THEN** a toast is shown and the code input is not marked invalid

#### Scenario: wrong confirmation code at enrolment

- **WHEN** the confirmation code is rejected
- **THEN** the confirm input is aria-invalid and described by the error message
