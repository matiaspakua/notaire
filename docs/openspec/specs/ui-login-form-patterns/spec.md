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

### Requirement: Login presents one desk for the task

The unauthenticated login page SHALL present one card (`data-testid="login-card"`)
that contains the task title, the credential form, and the submit control. The
page SHALL have exactly one `h1`, whose accessible name is Notaire. The
`login.title` message SHALL be an `h2` inside that card. The seal
(`data-testid="login-mark"`) SHALL enter using opacity and a 4px translate
whose duration and easing come from `frontend/src/theme/motion.ts`. The seal
SHALL NOT rotate and SHALL NOT use a spring transition. When the user prefers
reduced motion, the seal and the card SHALL render with no transform.

#### Scenario: Credentials sit on one desk card

- **GIVEN** the unauthenticated login page
- **WHEN** the page renders
- **THEN** `login-card` contains the task heading and `btn-sign-in`
- **AND** the only `h1` is Notaire

#### Scenario: Seal entrance does not rotate

- **GIVEN** the unauthenticated login page
- **WHEN** the seal has finished entering
- **THEN** its transform includes no rotation
- **AND** under `prefers-reduced-motion: reduce` the seal's transform is `none`

