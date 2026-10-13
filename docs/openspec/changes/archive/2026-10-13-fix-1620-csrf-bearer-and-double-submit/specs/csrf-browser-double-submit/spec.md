# csrf-browser-double-submit Specification

## Purpose

Define how the `/api/**` filter chain proves same-origin intent for
cookie-authenticated browser requests (double-submit cookie) while keeping
Bearer-token API clients working. Completes the CSRF enforcement introduced
by #1618 and #1619 for the two client types the product actually ships (CU78).

## ADDED Requirements

### Requirement: The API chain issues a script-readable token cookie

Every response produced by the `/api/**` filter chain, including the responses
that set or clear the HttpOnly session cookie, MUST carry a non-HttpOnly
`XSRF-TOKEN` cookie whose value is the raw CSRF token, so the same-origin BFF
client can read it from `document.cookie` without access to the JWT.

#### Scenario: A safe request issues the token cookie

- **WHEN** an authenticated browser performs a `GET` through the API chain
- **THEN** the response sets an `XSRF-TOKEN` cookie that is not `HttpOnly`

#### Scenario: Login returns the token cookie beside the session cookie

- **WHEN** a browser submits valid credentials to `POST /api/v1/users/login`
- **THEN** the response sets both the HttpOnly session cookie and the
  script-readable `XSRF-TOKEN` cookie, and the browser can immediately echo the
  latter on its next unsafe request

#### Scenario: The nonce stays stable while the session is unchanged

- **WHEN** an authenticated browser request already carries an `XSRF-TOKEN` cookie
- **THEN** the response does not replace it with a different value, so a client
  that reads the cookie immediately before sending a request stays in sync even
  while other requests are in flight

### Requirement: Unsafe cookie-authenticated requests must prove same-origin intent

Unsafe methods (other than `GET`, `HEAD`, `TRACE` and `OPTIONS`) authenticated by
the session cookie MUST present the raw `XSRF-TOKEN` cookie value in the
`X-XSRF-TOKEN` request header. The API chain MUST resolve a header-supplied value
verbatim, without the XOR mask applied to server-rendered tokens.

#### Scenario: Browser double-submit is accepted

- **WHEN** a cookie-authenticated browser sends an unsafe request through the
  Next proxy with `X-XSRF-TOKEN` equal to the `XSRF-TOKEN` cookie value
- **THEN** the API accepts the request and applies its normal authentication and
  authorization rules

#### Scenario: Cookie-authenticated request without the header is rejected

- **WHEN** a request authenticated by the session cookie omits `X-XSRF-TOKEN`
- **THEN** the API rejects it with `403` before the controller runs and no state
  changes

### Requirement: Bearer-token clients are exempt from CSRF

Requests that carry an `Authorization: Bearer` header MUST be exempt from CSRF
validation, because a credential the client must attach explicitly cannot be
replayed by a third-party site. The exemption MUST NOT apply to
cookie-authenticated requests.

#### Scenario: Bearer-token write succeeds without an XSRF token

- **WHEN** a client sends an unsafe request with a valid `Authorization: Bearer`
  token and no `X-XSRF-TOKEN` header
- **THEN** the API accepts it according to its existing authentication and
  authorization rules

#### Scenario: The exemption does not weaken the cookie flow

- **WHEN** the same unsafe endpoint is called with only the session cookie, no
  `Authorization` header and no `X-XSRF-TOKEN` header
- **THEN** the API still rejects it with `403`

### Requirement: Session bootstrap endpoints stay reachable without a token

`POST /api/v1/users/login` and `POST /api/v1/users/logout` MUST remain exempt
from CSRF validation so a session can be created before a token exists and torn
down after it is revoked.

#### Scenario: Logout succeeds without a CSRF token

- **WHEN** a client calls `POST /api/v1/users/logout` without an `X-XSRF-TOKEN`
  header
- **THEN** the API clears the session cookie and reports success
