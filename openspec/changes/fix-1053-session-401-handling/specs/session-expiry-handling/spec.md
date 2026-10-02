## Purpose

Define how the Notaire web client ends an authenticated session when the API
rejects the request with HTTP 401, so users re-authenticate via CU84 instead of
seeing repeated generic error toasts.

## ADDED Requirements

### Requirement: Session ends on authenticated 401

While the client believes the user is authenticated, an API response of HTTP 401
SHALL end the local session and send the user to the login screen with an
expired-session signal. Source: CU84 – Login al sistema (session post-condition).

#### Scenario: Authenticated request receives 401

- **WHEN** the client holds an authenticated session and any API call returns HTTP 401
- **THEN** local auth state is cleared and the user is navigated to `/login?expired=1`

#### Scenario: Login shows expired session message

- **WHEN** the login screen is opened with `expired=1` in the query string
- **THEN** the user sees a clear message that the session expired and must sign in again

#### Scenario: User can re-authenticate after expiry

- **WHEN** the user completes a successful login after a session-expiry redirect
- **THEN** the user reaches the dashboard authenticated and without the expiry message

### Requirement: Non-expiry errors stay local

HTTP failures other than authenticated-session 401 MUST NOT clear the session or
redirect to login. Bad credentials on the login form MUST NOT trigger the expiry
redirect loop.

#### Scenario: Non-401 API error does not logout

- **WHEN** an authenticated API call fails with a status other than 401 (for example 400, 403, 404, 409, 500)
- **THEN** the local session remains and the client does not navigate to `/login?expired=1`

#### Scenario: Login failure with 401 does not expiry-redirect

- **WHEN** an unauthenticated login attempt receives HTTP 401 (invalid credentials)
- **THEN** the login page shows a credentials error and does not redirect via the session-expiry path
