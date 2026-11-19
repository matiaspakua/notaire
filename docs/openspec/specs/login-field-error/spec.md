# login-field-error Specification

## Purpose
TBD - created by archiving change fix-2259-login-field-errors. Update Purpose after archive.

## Requirements

### Requirement: Blank login fields are field errors

When sign-in is submitted with a blank username or a blank password, each blank field SHALL show the required message, SHALL be aria-invalid, SHALL NOT show a toast for that check, and SHALL send no login request. Typing in a blank field SHALL clear that field's error.

#### Scenario: both fields blank

- **WHEN** sign-in is submitted with both fields blank
- **THEN** both inputs are aria-invalid with the described error, no toast is shown and no request is sent

#### Scenario: typing clears that field

- **WHEN** the user types a username after both fields were blank
- **THEN** the username input is no longer aria-invalid and the password input still is
