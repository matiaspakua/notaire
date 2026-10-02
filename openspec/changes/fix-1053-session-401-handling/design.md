> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`frontend/src/lib/api-client.ts` `handleResponse` throws `ApiError` for every
non-OK status, including 401. `frontend/src/lib/query-client.ts` has no global
`onError`. `useAuthStore.logout()` clears token and the `notaire-auth-status`
cookie but nothing invokes it on 401. JWT TTL remains 24 h server-side
(`JwtTokenService`). Related E2E gap: #690. Sibling audits #1051/#1052/#1054 are
out of scope.

## Goals / Non-Goals

**Goals:**

- Single shared path: authenticated 401 → logout → `/login?expired=1` + message.
- Cover with unit tests and Playwright (closes #690 acceptance).
- Keep login credential failures distinct from session expiry.

**Non-Goals:**

- HttpOnly cookie / CSP (#1051), admin guards (#1052), mutation toast mapping (#1054).
- Changing JWT TTL, refresh tokens, or backend SecurityFilterChain.
- Translating every API error body (only expiry messaging for this change).

## Decisions

1. **Centralize in the API client + QueryClient, not per page**
   - Why: 401 can come from any query/mutation; page-level handlers miss coverage
     (the same class of bug as #1054).
   - Alternative rejected: only React Query `onError` — raw `apiGet`/`apiPost`
     outside QueryClient would still ignore 401.
   - Approach: detect 401 once (preferably in `handleResponse` / a thin wrapper),
     call `logout()`, then redirect; QueryClient `onError` as a safety net for
     thrown `ApiError` from queries/mutations. Guard against re-entry and against
     treating login-endpoint failures as session expiry.

2. **Query flag `expired=1` on `/login`**
   - Why: matches issue AC; readable in Playwright; survives full navigation.
   - Alternative rejected: toast-only without URL signal (harder to assert; lost
     on refresh).

3. **Do not change middleware cookie semantics beyond logout already clearing it**
   - Why: expiry handling is API-driven; middleware already gates on
     `notaire-auth-status`. Clearing via `logout()` is enough for this issue.
   - #1051 may later replace this cookie model.

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Login 401 (bad password) triggers expiry redirect loop | Exclude `/usuarios/login` (and unauthenticated calls) from session-expiry handler |
| Concurrent 401s cause multiple redirects / toasts | Idempotent guard (flag or “already logging out”) |
| E2E hard to force real JWT expiry in 24 h | Prefer invalidating/removing token or stubbing API 401 in Playwright (#690 notes); document chosen approach in the E2E spec |
| Overlap with future #1051 cookie auth | Keep handler auth-store / ApiError based so cookie migration can keep the same UX contract |

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Authenticated request receives 401 | unit | `frontend/src/tests/unit/` (api-client or session-expiry helper) |
| Login shows expired session message | unit/component | login page test or focused RTL/vitest |
| Non-401 API error does not logout | unit | same unit suite |
| Login failure with 401 does not expiry-redirect | unit | login / handler unit |
| User can re-authenticate after expiry | E2E | `frontend/tests/e2e/TS-nnnn-session-expiry.spec.ts` |

- New unit tests (`frontend/src/tests/unit/`): session-expiry handler + login expired banner
- New integration tests (`src/test/java/.../integration/`): n/a — frontend-only
- Coverage impact (JaCoCo ratchet floor; 80% target): unchanged (no backend code)

## Regression Strategy

- Existing tests affected: `auth-store.test.ts` (logout still clears cookie);
  login E2E (`TS-0002-logout-authentication`, login specs) must stay green
- Full suite command: `mvn verify -pl backend-api` (sanity; no backend delta)
- Frontend: `cd frontend && npm test` (or project vitest script) + Playwright
- HTTP/Bruno API suite: n/a for this change (no API contract change)
- Legacy paths at risk: none (`frontend-swing` removed)

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`:
  - New `TS-nnnn-session-expiry.spec.ts` (allocate next free TS number; map in
    `E2E-TEST-MAPPING.md`; link CU84, #1053, #690)
- Golden path: authenticated user → forced 401 / expired token → land on
  `/login?expired=1` with message → successful re-login → dashboard
- Edge / error paths: non-401 failure does not logout; logout button path unchanged
- Viewports: 320px / 768px / 1024px for login expired message visibility
- Command: `cd frontend && npx playwright test TS-nnnn-session-expiry`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend-only deploy; no schema coupling
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): log in, clear/invalidate token in DevTools,
  navigate or trigger an API call, confirm redirect + message + re-login

## Rollback Strategy

- Revert safe: yes — pure client behavior; revert the PR
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: users again see generic toasts on expiry
  (pre-#1053 behavior)

## Migration Plan

None beyond shipping the frontend change after Gate 3/4.

## Open Questions

None blocking Gate 1. Implementation may choose “mutate stored token vs route
intercept stub” for Playwright; either satisfies the AC if the user lands on
`/login?expired=1` and can re-login.
