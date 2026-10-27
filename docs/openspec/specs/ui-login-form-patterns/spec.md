# ui-login-form-patterns Specification

## Purpose
Require the login page (CU84) to compose credentials with the mandated theme form patterns.
## Requirements
### Requirement: Login uses theme form patterns

The login page SHALL wrap the credentials form in `FormContainer`, SHALL place each
credential control inside `FormField`, and SHALL place the primary submit control
inside `FormActions` from `frontend/src/theme/form-patterns.tsx`. Field labels SHALL
use sentence case (not decorative ALL-CAPS). Existing `data-testid` values for
username, password, submit, and session-expired SHALL be preserved.

#### Scenario: Login fields are FormField-wrapped

- **GIVEN** the unauthenticated login page
- **WHEN** the page renders
- **THEN** the username and password controls are each inside a `FormField`
- **AND** the form root uses `FormContainer`

#### Scenario: Submit lives in FormActions

- **GIVEN** the unauthenticated login page
- **WHEN** the page renders
- **THEN** the sign-in control (`btn-sign-in`) is rendered inside `FormActions`

