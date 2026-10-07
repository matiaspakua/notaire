# login-lockout-e2e Specification

## Purpose
The account lockout is verified through the login form. Source: #689, #560; owner CU78.
## Requirements
### Requirement: Repeated failed logins lock the account in the UI

After the configured number of failed login attempts for a username, the next attempt MUST show the lockout message and MUST NOT authenticate, on desktop and on mobile widths.

#### Scenario: Lockout message on desktop

- **WHEN** wrong credentials are submitted repeatedly for one username at 1024 px width
- **THEN** the login page shows the lockout message and stays on `/login`

#### Scenario: Lockout message on mobile

- **WHEN** the same flow runs at 320 px width
- **THEN** the lockout message is visible and the form is still usable without horizontal scroll

#### Scenario: A locked account stays locked

- **WHEN** a locked username submits one more attempt
- **THEN** the lockout message is shown again and no session is created

