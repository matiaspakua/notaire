# Fix expired-session 401 handling on the frontend

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1053 |
| Use Case | CU84 – Login al sistema |
| Branch | `cursor/fix-1053_session-401-handling-69d3` |
| Gate 1 status | pending (artifacts complete; implement after #1126/#1128 merge) |

## Objetivo

When the JWT expires (24 h), the Next.js client keeps `isAuthenticated=true` in
localStorage and treats every subsequent `401` like a generic API failure. Users
see error toasts on every screen until they log out manually. This change restores
session end as an explicit CU84 outcome: clear local auth, redirect to login with
an expiry message, and cover it with Playwright (#690 gap).

## What Changes

- Global handling of HTTP `401` from the API client / React Query layer so a
  single code path calls logout and navigates to `/login?expired=1`.
- Login page shows a clear “session expired” message when `expired=1` is present.
- Playwright E2E proves expiry → login redirect → re-login (closes #690 AC gap).
- No backend JWT lifetime change; no HttpOnly-cookie migration (tracked in #1051).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An authenticated session ends when the server rejects the credential with 401 | CU84 – Login al sistema (post-condition: authenticated for current session) | Made explicit |
| After session end, the user must re-authenticate via the login screen | CU84 – Main flow steps 1–5 | Made explicit |
| The user is informed that the session expired (not a generic API error) | CU84 – Alternative flows (new 3e-style expiry path) | New (document in CU84) |

## Capabilities

### New Capabilities

- `session-expiry-handling`: Client clears auth and redirects to login with an
  expiry signal whenever the API returns 401 for an authenticated request.

### Modified Capabilities

- (none — no existing capability under `openspec/specs/` covers client session end)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | JWT already returns 401; no server change |
| `frontend` | yes | `api-client`, React Query defaults, login page, auth store usage, E2E |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Existing frontend/Playwright jobs cover this |

### Surface area

- Entities: none
- Endpoints: consumes existing authenticated REST (`401 Unauthorized`); no contract change
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none new

### Architecture review

Follows existing Zustand auth store + `api-client` / React Query stack. No ADR.
Does not implement #1051 (HttpOnly cookie / CSP) or #1052 (admin route guards).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU84 - Login.md` | Add alternative flow: session expired / 401 → re-login with message |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Map new TS-nnnn session-expiry E2E to CU84 / #1053 / #690 |
| `CHANGELOG.md` | User-visible: expired sessions redirect to login with a clear message |

## Out of Scope

- **#1054** — generic mutation error toasts / `extractApiError` on CRUD pages
- **#1051** — JWT out of localStorage; HttpOnly cookie; CSP nonce
- **#1052** — admin route role guards
- Changing JWT TTL (`JwtTokenService`) or refresh/revocation (#676)
- Backend auth filter behavior (already returns 401)
