## ADDED Requirements

### Requirement: Cookie-authenticated unsafe requests require CSRF proof

Browser requests authenticated by the HttpOnly session cookie SHALL present a
valid CSRF token for every unsafe API method. The application SHALL make that
token available to its same-origin BFF client without exposing the JWT.

#### Scenario: Unsafe cookie request without a CSRF token is rejected

- **WHEN** a browser-origin unsafe API request presents the auth cookie but omits
  a valid CSRF token
- **THEN** the API rejects the request before it changes application state

#### Scenario: Same-origin browser write includes a valid CSRF token

- **WHEN** an authenticated browser submits an unsafe request through the Next
  proxy with the issued CSRF token
- **THEN** the API accepts the request according to its existing authentication
  and authorization rules

#### Scenario: JWT remains unreadable to browser script

- **WHEN** the browser obtains a CSRF token for an authenticated session
- **THEN** the JWT session cookie remains HttpOnly and is not exposed to
  `document.cookie`
