# API Authentication & Authorization Guide

This guide documents the authentication and authorization mechanisms implemented in Notaire.

## Overview

Notaire uses **JWT (JSON Web Tokens)** for stateless API authentication, implemented via JJWT 0.13.0 and Spring Security 6.x. Clients authenticate via `POST /api/v1/usuarios/login`. The browser session receives an **HttpOnly** cookie `notaire-auth-token` (issue #1051); API tooling still receives a JSON `token` for `Authorization: Bearer`. Every other `/api/**` request **must** present a valid cookie **or** Bearer token — otherwise `401 Unauthorized` (see issues #552 / #1051).

## Architecture

```text
Client ──POST /api/v1/usuarios/login──► UsuarioController
                                              │
                                    ──────────▼──────────
                                    │ passwordMatches()   │
                                    │ BCrypt (legacy MD5  │
                                    │ auto-migrated)      │
                                    └──────────┬──────────┘
                                               │
                                    ──────────▼──────────
                                    │  JwtTokenService   │
                                    │  generateToken()   │
                                    └──────────┬──────────┘
                                               │
                                      { valido: true,
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
| `JwtAuthenticationFilter` | `config/` | Extract Bearer **or** auth cookie, set `SecurityContext` |
| `AuthCookieService` | `config/` | Build/clear HttpOnly session cookie (`COOKIE_SECURE`) |
| `SecurityAndCorsConfig` | `config/` | Security filter chain — API chain registers the JWT filter; login/logout permitAll |

### Client-side session propagation

| Client | Credential channel |
|--------|--------------------|
| Next.js dashboard | HttpOnly cookie via same-origin `/api/v1` rewrite + `credentials: 'include'`. Zustand persists user profile only — **never** the JWT (`localStorage` `notaire-auth`). Logout calls `POST /usuarios/logout`. UX cookies `notaire-auth-status` / `notaire-auth-role` remain non-credential (#1052). |
| Bruno / HTTP / OpenAPI | JSON `token` from login → `Authorization: Bearer` (unchanged) |

Production frontend CSP uses a per-request nonce for `script-src` and forbids
`'unsafe-eval'` (issue #1051).

### Token generation

```java
// JwtTokenService.generateToken(username)
return Jwts.builder()
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
  "valido": true,
  "idUsuario": 1,
  "nombre": "admin",
  "tipo": "Escribano",
  "estado": true,
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

## Role-Based Access Control (RBAC)

### Role model

```text
Usuario ──M:1──► Rol
Rol     ──name, description──► (ENUM: Escribano, Secretario, Admin, ...)
```

The `Rol` entity is stored in the `roles` table. `UsuarioController` exposes the role in the `UsuarioResponse` DTO.

### Current state

`apiSecurityFilterChain` requires `authenticated()` on every `/api/**` request except `POST /api/v1/usuarios/login`, `POST /api/v1/usuarios/logout`, and CORS preflight (`OPTIONS`). Any request without a valid cookie or Bearer token gets `401` before reaching a controller. This is coarse-grained (authenticated vs. not) — there is no per-role authorization yet. CSRF stays disabled; browser traffic is same-origin via the Next proxy with SameSite=Lax cookies.

### Extending RBAC

1. Add permissions/modules to the `Rol` entity.
2. Replace `.anyRequest().authenticated()` with per-route `.hasRole("ADMIN")` etc. in `SecurityAndCorsConfig`.
3. Pass the role claim in the JWT payload.

## Authentication error handling

| Scenario | HTTP | Response body |
|----------|------|---------------|
| Wrong password | 200 | `{ "valido": false }` |
| User not found | 200 | `{ "valido": false }` |
| Inactive user | 200 | `{ "valido": false }` |
| DB error | 200 | `{ "valido": false }` |
| Missing/invalid/expired JWT on a protected endpoint | 401 | `Unauthorized` (via `apiAuthenticationEntryPoint`) |

The login endpoint always returns HTTP 200 to avoid information leakage. The `valido` field in the response distinguishes success from failure.

## Token refresh strategy

Currently tokens are single-use with a 24-hour TTL (configurable). There is no refresh endpoint. Re-login is required when the token expires. If shorter TTLs are needed, add a `POST /api/v1/usuarios/token/refresh` endpoint that accepts a valid token and returns a new one.

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
