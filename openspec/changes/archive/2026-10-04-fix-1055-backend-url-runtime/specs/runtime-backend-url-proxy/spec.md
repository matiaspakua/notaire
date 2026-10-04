<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Provide a request-time same-origin BFF for `/api/v1/**` driven by runtime server
configuration, and ensure the public login page never discloses internal backend
URLs. Source: #1055; owner CU78 – Security, Privacy and Compliance.

## ADDED Requirements

### Requirement: Backend upstream URL is resolved at request time

The frontend MUST proxy browser calls to `/api/v1/**` through a server-side
mechanism (App Router Route Handler or equivalent Node runtime proxy) that reads
the upstream base URL from server environment (`BACKEND_URL`) at request time.
Build-time `next.config` rewrite destinations MUST NOT be the sole mechanism that
selects the upstream host, so a single standalone image can run against different
backends by changing runtime env only.

#### Scenario: Proxy uses runtime BACKEND_URL

- **WHEN** the frontend server handles a request to `/api/v1/<path>` and
  `BACKEND_URL` is set in the process environment
- **THEN** the upstream request is sent to `{BACKEND_URL}/<path>` (or the
  documented equivalent join) using the value observed at request handling time,
  not a host string baked exclusively during `next build`

#### Scenario: Missing BACKEND_URL fails safely

- **WHEN** the proxy handles an `/api/v1/**` request and no usable server-side
  backend base URL is configured
- **THEN** the handler MUST NOT silently target an arbitrary internal hostname
  from a client-public env var as the long-term contract, and MUST fail in a
  controlled way (documented error response / log) without rendering the
  upstream URL on the login page

#### Scenario: One image works across environments

- **WHEN** the same frontend build artifact/image is started with different
  runtime `BACKEND_URL` values
- **THEN** `/api/v1/**` traffic is forwarded to the corresponding upstream without
  requiring a rebuild to change the destination host

### Requirement: Public login page does not disclose backend URL

The `/login` page MUST NOT display `BACKEND_URL`, Docker-internal hostnames
(e.g. `backend:8080`), or `NEXT_PUBLIC_API_URL` values to anonymous visitors.

#### Scenario: Login page has no Backend URL line

- **WHEN** an anonymous user opens `/login`
- **THEN** the rendered page MUST NOT contain a “Backend:” infrastructure URL
  line or equivalent disclosure of the API upstream address

#### Scenario: Login HTML omits internal Docker API host

- **WHEN** the login page is served from a deployment that uses an internal
  hostname such as `http://backend:8080/api/v1` as the server-side upstream
- **THEN** that internal hostname MUST NOT appear in the login page document
  text or hydrated client output

### Requirement: HttpOnly cookie auth through the BFF is preserved

Browser sessions MUST continue to authenticate via the HttpOnly JWT cookie path
introduced by #1051: same-origin `/api/v1` calls with credentials included, cookie
forwarding through the BFF, and no return to script-readable JWT storage for the
session credential.

#### Scenario: Cookie header is forwarded upstream

- **WHEN** the browser calls `/api/v1/**` with credentials and a session cookie
- **THEN** the BFF forwards the `Cookie` header (including the HttpOnly auth
  cookie) to the upstream backend

#### Scenario: Set-Cookie from login/logout is relayed to the browser

- **WHEN** the upstream backend responds with `Set-Cookie` for the auth session
  (login or logout/clear)
- **THEN** the BFF relays those `Set-Cookie` header(s) to the browser response
  so HttpOnly session semantics remain intact

#### Scenario: Auth E2E login path stays green

- **WHEN** Playwright auth golden paths (login / authenticated API / logout)
  run against the frontend with the request-time BFF
- **THEN** those tests pass without reintroducing `localStorage` JWT session
  storage as the credential

### Requirement: Edge interceptor continues to skip API proxy paths

Whether the edge file is `middleware.ts` or `proxy.ts` (#1056), requests under
`/api/**` MUST bypass edge auth redirects so the BFF can forward credentials.

#### Scenario: API paths are not redirected to login by the edge interceptor

- **WHEN** a request targets a path under `/api/`
- **THEN** the edge interceptor MUST NOT redirect it to `/login` (request
  proceeds to the request-time BFF)
