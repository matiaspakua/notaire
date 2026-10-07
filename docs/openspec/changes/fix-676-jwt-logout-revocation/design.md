# Design

## Context

Assessment #1242 lists JWT without refresh or revocation (#676). Expiry is already enforced and deactivated users already lose access on the next request (#559); revocation on logout was missing.

## Goals / Non-Goals

Goal: a logged-out token cannot be replayed. Non-goals: revoking sessions on password change, refresh tokens, shorter lifetimes (owner decision, stays in #676).

## Decisions

Per-token `jti` denylist in PostgreSQL (survives restarts and works with several instances, unlike the in-memory login lockout). Stores ids only, never tokens. Purge on logout instead of a scheduler. Tokens issued before V43 have no `jti`: not revocable, they expire within 24 h.

## Riesgos / Trade-offs

One extra indexed lookup per authenticated request. Tokens issued before the deploy cannot be revoked until they expire. `session-expiry` calls logout after a 401, which revokes nothing because the token is already invalid.

## Testing Strategy

Integration test (H2) observed failing first (token still 200 after logout); unit tests for the token id, the service and the filter. Smoke-tested against PostgreSQL 17 with V43 applied: Bearer and cookie replay return 401, the second session 200, expired rows purged.

## Regression Strategy

Full backend suite (2077 tests), `JwtAuthIntegrationTest`, `RbacIntegrationTest`, Playwright logout specs (TS-0002/0060/0070/0071 log in through the UI, so they never revoke the shared admin token).

## Playwright Strategy

No UI change; Playwright not affected.

## Deployment Strategy

Flyway V43 creates `revoked_tokens`; no configuration. Sessions open at deploy keep working.

## Rollback Strategy

Revert the commit.
