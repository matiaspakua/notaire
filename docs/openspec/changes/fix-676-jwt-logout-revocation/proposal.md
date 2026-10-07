# Logout revokes the presented JWT server-side (slice of #676)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #676 |
| Use Case | CU84 – Login |
| Branch | `fix/676_jwt_logout_revocation` |
| Gate 1 status | draft |

## Objetivo

Logout only cleared the HttpOnly cookie, so a token copied before logout (from a Bearer client, logs, a shared machine) stayed valid for up to 24 hours. Make logout revoke the presented token on the server without changing the logout contract and without ending the user's other sessions. Password-change revocation and the refresh/lifetime policy stay in #676.

## What Changes

- `JwtTokenService` adds a random `jti` to every token and exposes `extractTokenId` / `extractExpiration`.
- New `revoked_tokens` table (Flyway `V43`), `RevokedToken` entity and `RevokedTokenRepository`.
- New `TokenRevocationService`: `revoke(token)` stores the id until the token's expiry (purging expired rows), `isRevoked(token)`.
- `JwtAuthenticationFilter` skips revoked tokens (401 on protected endpoints); token extraction moves to `RequestTokenResolver`, shared with logout.
- `POST /api/v1/usuarios/logout` revokes the presented token (Bearer or cookie) before clearing the cookie; request and response unchanged, OpenAPI description updated.
- Data dictionary and ERD regenerated; API authentication guide and CHANGELOG updated.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A token presented at logout can no longer authenticate, even before it expires | #676 | New |
| Logout revokes only the presented token; other sessions of the same user stay valid | #676 | New |
| Logout without a token or with an invalid token still answers `200 {"ok": true}` | #1051 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `jwt-logout-revocation`: Logout revokes the presented JWT on the server.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `JwtTokenService`, `JwtAuthenticationFilter`, `TokenRevocationService`, `RequestTokenResolver`, `UserController`, `RevokedToken`, V43 |
| `frontend` | no | already calls logout with the cookie |
| Docs / scripts / CI | yes | OpenAPI description, data dictionary, ERD, auth guide, CHANGELOG |

### Surface area

- Endpoints: `POST /api/v1/usuarios/logout` (behaviour: revokes the token; contract unchanged)
- Entities / Flyway: `RevokedToken`, `V43__create_revoked_tokens.sql`
- Configuration: none

### Architecture review

Revocation is a denylist of token ids checked by the existing JWT filter (one primary-key lookup per authenticated request, next to the user lookup the filter already does). Per-user token versions were rejected: they would end every session of the user (the E2E suite shares one admin token).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Security entry |
| `backend-api/openapi/openapi.yaml` | logout description regenerated |
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | `jti`, revocation section |
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` and `ERD/` | `revoked_tokens` |
