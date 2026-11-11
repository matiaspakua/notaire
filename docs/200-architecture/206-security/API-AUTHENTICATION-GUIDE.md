# API Authentication & Authorization Guide

This guide documents the authentication and authorization mechanisms implemented in Notaire.

## Overview

Notaire uses **JWT (JSON Web Tokens)** for stateless API authentication, implemented via JJWT 0.13.0 and Spring Security 6.x. Clients authenticate via `POST /api/v1/users/login`. The browser session receives an **HttpOnly** cookie `notaire-auth-token` (issue #1051); API tooling still receives a JSON `token` for `Authorization: Bearer`. Every other `/api/**` request **must** present a valid cookie **or** Bearer token — otherwise `401 Unauthorized` (see issues #552 / #1051).

## Architecture

```text
Client ──POST /api/v1/users/login──► UserController
                                              │
                                    ──────────▼──────────
                                    │ passwordMatches()   │
                                    │ BCrypt only (#1762) │
                                    └──────────┬──────────┘
                                               │
                                    ──────────▼──────────
                                    │  JwtTokenService   │
                                    │  generateToken()   │
                                    └──────────┬──────────┘
                                               │
                                      { valid: true,
                                        token: "eyJ..." }  + Set-Cookie: notaire-auth-token
```

On protected requests:

```text
Browser ──Cookie: notaire-auth-token──► Next proxy ──► JwtAuthenticationFilter
API client ──Authorization: Bearer <token>──────────► JwtAuthenticationFilter
                                                 │
                                       isValid(token)?  (Bearer preferred if both)
                                       extractUsername(token)
                                       SecurityContextHolder.setAuth()
                                                 │
                                          ► Controller
```

## JWT Implementation

### Token structure

| Claim | Value |
|-------|-------|
| `jti` | random UUID identifying this token, used for logout revocation (issue #676) |
| `sub` | username (e.g., `admin`) |
| `iat` | issued-at timestamp |
| `exp` | expiry timestamp (`iat + jwt.expiration-ms`) |

Signing algorithm: **HS256** (HMAC-SHA256).

### Configuration properties

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration-ms: 86400000  # 24 hours
```

`jwt.secret` has **no default** — the application fails fast at startup
(`JwtTokenService#validateSecret`, `@PostConstruct`) if it is blank, shorter
than 32 bytes, or left as the old hardcoded value that used to ship in this
repo (issue #558: that value was a real, git-committed signing key, letting
anyone with repo read access forge valid tokens for any user). Set
`JWT_SECRET` in `.env` (see `.env.example`); generate one with
`openssl rand -base64 48`. Never commit a real secret.

### Key classes

| Class | Location | Responsibility |
|-------|----------|---------------|
| `JwtTokenService` | `config/` | Generate, validate, and parse tokens |
| `JwtAuthenticationFilter` | `config/` | Extract Bearer **or** auth cookie, skip revoked tokens, set `SecurityContext` |
| `TokenRevocationService` | `config/` | Revoke the presented token on logout; answer whether a token was revoked (`revoked_tokens`) |
| `AuthCookieService` | `config/` | Build/clear HttpOnly session cookie (`COOKIE_SECURE`) |
| `SecurityAndCorsConfig` | `config/` | Security filter chain — API chain registers the JWT filter; login, login/mfa and logout permitAll |

### Client-side session propagation

| Client | Credential channel |
|--------|--------------------|
| Next.js dashboard | HttpOnly cookie via same-origin `/api/v1` rewrite + `credentials: 'include'`. Zustand persists user profile only — **never** the JWT (`localStorage` `notaire-auth`). Logout calls `POST /users/logout`. UX cookies `notaire-auth-status` / `notaire-auth-role` remain non-credential (#1052). |
| Bruno / HTTP / OpenAPI | JSON `token` from login → `Authorization: Bearer` (unchanged) |

Production frontend CSP uses a per-request nonce for `script-src` and forbids
`'unsafe-eval'` (issue #1051).

### Token generation

```java
// JwtTokenService.generateToken(username)
return Jwts.builder()
    .id(UUID.randomUUID().toString())   // jti, see Logout and revocation
    .subject(username)
    .issuedAt(new Date())
    .expiration(new Date(System.currentTimeMillis() + expirationMs))
    .signWith(signingKey())
    .compact();
```

### Token validation

```java
// isValid() returns false for: null/blank, expired, tampered, malformed
boolean valid = jwtTokenService.isValid(token);
```

### Login response

```json
{
  "valid": true,
  "idUser": 1,
  "name": "admin",
  "type": "Escribano",
  "status": true,
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Second factor (TOTP, ADR-032, #1640)

`notaire.mfa.policy` (`NOTAIRE_MFA_POLICY`) is `OFF`, `OPTIONAL` (default) or `ENFORCE`. `ENFORCE` also needs
`NOTAIRE_MFA_ENCRYPTION_KEY` (startup fails without it) and applies to the roles in `notaire.mfa.required-roles`
(default `ADMIN,NOTARY`). `OFF` is the break-glass switch if locked out.

After a valid password, `POST /api/v1/users/login` returns one of three bodies:

| Case | Body | Session cookie / `token` |
|------|------|--------------------------|
| No second factor needed | the success body above | yes |
| Confirmed enrolment (any role, policy not `OFF`) | `{"valid":false,"mfaRequired":true,"mfaEnrollmentRequired":false,"mfaToken":"..."}` | no |
| `ENFORCE`, ADMIN/NOTARY without enrolment | `{"valid":false,"mfaRequired":false,"mfaEnrollmentRequired":true,"mfaToken":"..."}` | no |

`valid` stays `false` so clients that predate MFA read it as a failed login. `mfaToken` is a 5-minute scoped JWT
(`notaire.mfa.challenge-ttl-ms`); it is never accepted as a session.

```text
mfaRequired:            POST /api/v1/users/login/mfa {"mfaToken","code"|"recoveryCode"} -> success body (session)
mfaEnrollmentRequired:  POST /api/v1/mfa/enrollment          (Bearer mfaToken) -> {"secret","otpauthUri"}
                        POST /api/v1/mfa/enrollment/confirm  (Bearer mfaToken) {"code"} -> {"recoveryCodes":[10]}
                        then log in again (password, then code)
signed in:              POST /api/v1/mfa/disable {"code"|"recoveryCode"} -> 204   (also enrolment begin/confirm)
```

The enrolment token authenticates only `/api/v1/mfa/enrollment/**`. Responses carrying the secret or recovery
codes are `Cache-Control: no-store`; store the codes, they are shown once. A wrong code at `login/mfa`,
`enrollment/confirm` or `disable` returns `valid:false` / `400` and counts toward the same lockout as a wrong
password (429 once locked); the password step does not reset that counter, only a completed login does. Logs and
metrics (`login{status="mfa_failed"}`) never contain code values.

### Web client flow (#1640, slice 3)

The login page (`app/login`) renders the outcome of `resolveLoginOutcome` (`core/auth`): a session is stored only when
`valid` is true; `mfaRequired` shows the code step (6-digit TOTP or a recovery code, `POST /users/login/mfa`);
`mfaEnrollmentRequired` runs the enrolment panel with `mfaToken` as Bearer (`apiPost` option `bearerToken`, held in
component state only), then returns to the password step. Signed-in users enrol and disable at
`/dashboard/security` (sidebar footer). The UI shows the Base32 secret and the `otpauth://` URI; it renders no QR
image. The API has no enrolment-status endpoint, so the page cannot show whether MFA is on (409 on begin means
already enrolled). Playwright: `TS-0046-mfa-totp-enrol-login.spec.ts`, whose workflow sets a CI-only
`NOTAIRE_MFA_ENCRYPTION_KEY`.

## Role-Based Access Control (RBAC)

### Role model

```text
User ──M:1──► Role
Role     ──name, description──► (stored names such as ADMIN, ESCRIBANO, and SECRETARIA)
```

The `Role` entity is stored in the `roles` table. `UserController` exposes the role in the `UserResponse` DTO.

### Current state

`apiSecurityFilterChain` allows `POST /api/v1/users/login`, `POST /api/v1/users/logout` and CORS preflight (`OPTIONS`) without credentials and requires an authenticated user for everything else. A request without a valid cookie or Bearer token gets `401` before reaching a controller. CSRF protection is enabled with the double-submit cookie pattern (CU78 / #1618): every response issues a script-readable `XSRF-TOKEN` cookie that unsafe requests must echo in the `X-XSRF-TOKEN` header, and the nonce stays stable while the session is unchanged. Login/logout and Bearer-token clients are exempt (#1620); browser traffic is same-origin via the Next proxy with SameSite=Lax cookies.

Authorization (#559): `JwtAuthenticationFilter` asks `UserAuthorityResolver` for the authority of the token's user on every request. Active users of type Administrador or Admin (case-insensitive) get `ROLE_ADMIN`; active users of type Escribano get `ROLE_NOTARY` (no administration, #1638); other active users get `ROLE_USER`; a deleted or inactive user is not authenticated and gets `401`.

| Scope | Methods | Required |
|-------|---------|----------|
| `/api/v1/users/**` (except login, logout), `/api/v1/roles/**`, `/api/v1/audit-log/**` | all | `ROLE_ADMIN` |
| `/api/v1/workflow-definition/**`, `/api/v1/workflow-node/**`, `/api/v1/workflow-transition/**`, `/api/v1/procedure-types/**`, `/api/v1/document-types/**`, `/api/v1/folio-types/**`, `/api/v1/identification-types/**`, `/api/v1/concepts/**`, `/api/v1/management-statuses/**`, `/api/v1/procedure-templates/**`, `/api/v1/budget-templates/**`, `/api/v1/document-cost-templates/**` | POST, PUT, PATCH, DELETE | `ROLE_ADMIN` |
| the same catalogs | GET | any authenticated user |
| `/api/v1/history/{id}` (management history, the CU13 audit trail; issue #1250) | PUT, DELETE | `ROLE_ADMIN` |
| everything else | all | any authenticated user |

A non-administrator receives `403` from `apiAccessDeniedHandler`. The frontend mirrors the rule for the administration and audit screens (`frontend/src/lib/admin-access.ts`); the server is the authority.

### Extending RBAC

1. Add a path to `ADMIN_ONLY_PATHS` or `ADMIN_WRITE_PATHS` (or a method-specific rule such as `ADMIN_HISTORY_METHODS`) in `SecurityAndCorsConfig` and a case to `RbacIntegrationTest` and the Bruno `rbac/` folder.
2. For finer permission rules, use the `role_modules` rows through `UserAuthorityResolver` (not yet implemented).

## Authentication error handling

| Scenario | HTTP | Response body |
|----------|------|---------------|
| Wrong password | 200 | `{ "valid": false }` |
| User not found | 200 | `{ "valid": false }` |
| Inactive user | 200 | `{ "valid": false }` |
| DB error | 200 | `{ "valid": false }` |
| Account locked after failed attempts | 429 | `{ "valid": false, "message": "Account temporarily locked after too many failed attempts." }` |
| Missing/invalid/expired JWT on a protected endpoint | 401 | `Unauthorized` (via `apiAuthenticationEntryPoint`) |
| Valid JWT of a deleted or inactive user | 401 | `Unauthorized` |
| JWT revoked by logout | 401 | `Unauthorized` |
| Authenticated non-administrator on an administrator-only endpoint | 403 | `Forbidden` (via `apiAccessDeniedHandler`) |

A failed login for a wrong password, an unknown user, an inactive user, or a database error returns HTTP 200 with `valid` false. A locked account returns HTTP 429 with `valid` false and a lock message.

## Logout and revocation

`POST /api/v1/users/logout` revokes the token the request presents (Bearer first, then the auth cookie) and clears the cookie (issue #676). Revocation stores only the token id (`jti`) and its expiry in `revoked_tokens` (Flyway V43); `JwtAuthenticationFilter` ignores any token whose id is listed, so a copied token cannot be replayed after logout. Only that token is revoked: other sessions of the same user keep working. Rows are purged once the token has expired, on the next logout. Logout without a token, or with an invalid one, still answers `200 {"ok": true}`.

Not covered yet (issue #676): changing a password does not end existing sessions, and tokens issued before V43 carry no `jti`, so they cannot be revoked and simply expire.

## Token refresh strategy

Currently tokens are single-use with a 24-hour TTL (configurable). There is no refresh endpoint. Re-login is required when the token expires. If shorter TTLs are needed, add a `POST /api/v1/users/token/refresh` endpoint that accepts a valid token and returns a new one.

## Security checklist

- [ ] Change `jwt.secret` before deploying to production
- [ ] Use HTTPS in production (see [Deployment Guide — Production Considerations](../209-deployment/README.md#production-considerations))
- [ ] Set `jwt.expiration-ms` appropriate for your threat model
- [ ] Log all login failures (done via Micrometer + Prometheus)
- [ ] Monitor `notaire_operation_total{operation="login",status="bad_credentials"}` for brute-force

## Related documentation

- [`SQL-INJECTION-PREVENTION.md`](SQL-INJECTION-PREVENTION.md)
- [`INPUT-VALIDATION-STRATEGY.md`](INPUT-VALIDATION-STRATEGY.md)
- `infra/observability/grafana/provisioning/dashboards/notaire-auth.json` — login metrics dashboard
- `infra/observability/prometheus/alert-rules.yml` — brute-force alert rules
