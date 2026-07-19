# API Authentication & Authorization Guide

This guide documents the authentication and authorization mechanisms implemented in Notaire.

## Overview

Notaire uses **JWT (JSON Web Tokens)** for stateless API authentication, implemented via JJWT 0.12.6 and Spring Security 6.x. Clients (the Next.js dashboard and the Swing desktop client) authenticate via the `/api/v1/usuarios/login` endpoint and receive a JWT token on success. Every other `/api/**` request **must** include that token as a Bearer header — requests without a valid token are rejected with `401 Unauthorized` (see issue #552).

## Architecture

```
Client ──POST /api/v1/usuarios/login──► UsuarioController
                                              │
                                    ──────────▼──────────
                                    │ validateCredentials │
                                    │ MD5(password) check │
                                    └──────────┬──────────┘
                                               │
                                    ──────────▼──────────
                                    │  JwtTokenService   │
                                    │  generateToken()   │
                                    └──────────┬──────────┘
                                               │
                                      { valido: true,
                                        token: "eyJ..." }
```

On protected requests:
```
Client ──Authorization: Bearer <token>──► JwtAuthenticationFilter
                                                 │
                                       isValid(token)?
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
  secret: notaire-default-secret-key-change-in-production-!!
  expiration-ms: 86400000  # 24 hours
```

Override in production via environment variable or `application-prod.yml`. Never commit a real secret.

### Key classes

| Class | Location | Responsibility |
|-------|----------|---------------|
| `JwtTokenService` | `config/` | Generate, validate, and parse tokens |
| `JwtAuthenticationFilter` | `config/` | Extract Bearer token, set `SecurityContext` |
| `SecurityAndCorsConfig` | `config/` | Security filter chain — API chain registers the JWT filter |

### Client-side token propagation

Both clients capture the `token` field from the login response and attach it as
`Authorization: Bearer <token>` on every subsequent request:

| Client | Token capture | Header attachment |
|--------|---------------|--------------------|
| Next.js dashboard | `useAuthStore` (`frontend/src/store/auth-store.ts`) persists `token` alongside the user | `frontend/src/lib/api-client.ts`'s `buildHeaders()` reads the persisted token and sets `Authorization` |
| Swing desktop client | `RestClient.login()` (`frontend-swing/.../api/client/RestClient.java`) stores the token from the response DTO in a static field via `setAuthToken()` | `RestClient`'s private request builders (`makeGetRequest`, `makePostRequest`, `makePutRequest`, `makeDeleteRequest`, `makeGetRequestBytes`) call `applyAuthHeader()` before connecting |

The shared `DtoUsuario` (`notaire-shared`) and the TypeScript `DtoUsuario` type
both carry an optional `token` field, populated only in the login response.

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

```
Usuario ──M:1──► Rol
Rol     ──name, description──► (ENUM: Escribano, Secretario, Admin, ...)
```

The `Rol` entity is stored in the `roles` table. `UsuarioController` exposes the role in the `UsuarioResponse` DTO.

### Current state

`apiSecurityFilterChain` requires `authenticated()` on every `/api/**` request except `POST /api/v1/usuarios/login` and CORS preflight (`OPTIONS`). Any request without a valid Bearer token gets `401` before reaching a controller. This is coarse-grained (authenticated vs. not) — there is no per-role authorization yet.

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
- [ ] Use HTTPS in production (see HTTPS/TLS guide)
- [ ] Set `jwt.expiration-ms` appropriate for your threat model
- [ ] Log all login failures (done via Micrometer + Prometheus)
- [ ] Monitor `notaire_operation_total{operation="login",status="bad_credentials"}` for brute-force

## Related documentation

- `docs/04-operations/03-security/SQL-INJECTION-PREVENTION.md`
- `docs/04-operations/03-security/INPUT-VALIDATION-STRATEGY.md`
- `infra/grafana/provisioning/dashboards/notaire-auth.json` — login metrics dashboard
- `infra/prometheus/alert-rules.yml` — brute-force alert rules
