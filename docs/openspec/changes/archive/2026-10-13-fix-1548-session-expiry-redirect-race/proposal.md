# The expired-session redirect keeps `?expired=1` under load (TS-0093 red on main) (#1548)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1548 |
| Use Case | CU84 – Login al sistema (expired-session alternate flow) |
| Branch | `fix/1548_session_expiry_redirect_race` |
| Gate 1 status | draft |

## Objetivo

After a 401 the client goes to `/login?expired=1`, but under load the user sometimes landed on a plain `/login` with no 'session expired' message. A request trail showed the edge proxy answering `/login?expired=1` with 307 `/dashboard` while the UX status cookie was still present, and then `/dashboard` with 307 `/login`. TS-0093 failed on main run 38227311844, and locally 3 of 60 runs failed under `--workers=6`.

## What Changes

- `frontend/src/proxy.ts`: `/login?expired=1` is always let through and clears the stale `notaire-auth-status` / `notaire-auth-role` markers instead of bouncing to `/dashboard`.
- `lib/session-expiry.ts` exports `isSessionExpiryInProgress()`. The dashboard layout guard skips its own `router.replace("/login")` while an expiry redirect is in flight.
- TS-0093: leaves the dashboard before corrupting the cookie, polls the settled URL instead of racing `waitForURL` with the aborted `goto`, and waits for the login page to settle before re-login.
- Tests: Vitest `edge-proxy.test.ts` (new case) and `dashboard-layout-session-expiry.test.tsx`. CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An expired session always lands on the expired-login message | CU84 alternate flow; #1053 | Fix (was timing-dependent) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `session-expiry`: Expired-session handling in the frontend.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | edge proxy, dashboard guard, session expiry |
| `testing/e2e` | yes | TS-0093 robustness |

### Surface area

- Endpoints / entities / Flyway / configuration: none
- Edge proxy: `/login?expired=1` no longer redirects to `/dashboard`

### Architecture review

Edge-proxy routing and the client auth guard only; the backend and the HttpOnly JWT cookie are untouched.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
