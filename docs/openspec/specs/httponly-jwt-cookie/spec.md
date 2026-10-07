# httponly-jwt-cookie Specification

## Purpose

Deliver the browser session JWT as an HttpOnly cookie (set/cleared by the
backend, forwarded by the Next proxy) so page scripts cannot read the credential.
Source: #1051; owners CU78 – Security and Compliance, CU84 – Login.

## Requirements

### Requirement: Login establishes an HttpOnly session cookie

Successful `POST /api/v1/usuarios/login` MUST set a session cookie that carries
the JWT with attributes `HttpOnly`, `SameSite` of `Lax` or `Strict`, and
`Secure` in production (HTTPS) deployments. The cookie MUST NOT be readable from
`document.cookie`.

#### Scenario: Login sets HttpOnly Secure SameSite cookie

- **WHEN** a user completes a successful login through the application
- **THEN** the response includes a `Set-Cookie` for the auth token cookie with
  `HttpOnly` and `SameSite` set, and with `Secure` when the deployment is
  production/HTTPS

#### Scenario: JWT is not stored in localStorage after login

- **WHEN** a user completes a successful browser login
- **THEN** `localStorage` MUST NOT contain the JWT (the persisted `notaire-auth`
  payload has no usable `token` field)

### Requirement: Next proxy forwards the session cookie

Browser calls to `/api/v1/**` on the Next origin MUST include the auth cookie, and
the Next proxy/rewrite (or BFF) MUST forward that cookie to the backend so the API
can authenticate the request without a script-supplied `Authorization` header.

#### Scenario: Browser API call authenticates via proxy and cookie

- **WHEN** an authenticated browser session calls a protected `/api/v1/**` path
  through the Next proxy without attaching `Authorization` from JavaScript
- **THEN** the backend accepts the request based on the forwarded HttpOnly cookie

### Requirement: Backend accepts cookie or Bearer

`JwtAuthenticationFilter` (or successor) MUST authenticate a request that presents
either a valid auth cookie or a valid `Authorization: Bearer` token.

#### Scenario: API accepts cookie without Bearer

- **WHEN** a protected API request includes a valid auth cookie and omits
  `Authorization`
- **THEN** the request is authenticated and is not rejected with 401 solely for
  missing Bearer

#### Scenario: API still accepts Bearer for non-browser clients

- **WHEN** a protected API request includes a valid `Authorization: Bearer` token
  and no auth cookie
- **THEN** the request is authenticated as today (Bruno / integration clients)

### Requirement: Logout clears the HttpOnly cookie

Logout MUST cause an HTTP response that clears the auth token cookie
(`Max-Age=0` or equivalent). Client-only state clearing is not sufficient.

#### Scenario: Logout clears auth cookie

- **WHEN** an authenticated user logs out
- **THEN** subsequent browser requests do not present a valid auth token cookie
  and protected API calls fail authentication until login succeeds again
