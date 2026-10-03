> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1051 (audit-2026-09, CU78 + CU84): browser JWT lives in script-readable
storage and production CSP allows `unsafe-inline` / `unsafe-eval` for scripts.

Verified on workspace tip (2026-10-03):

| Location | Finding |
|----------|---------|
| `frontend/src/store/auth-store.ts` | Zustand `persist` name `notaire-auth` partializes `token` |
| `frontend/src/lib/api-client.ts` | Reads `localStorage` JWT → `Authorization: Bearer` |
| `frontend/next.config.ts` headers | `script-src 'self' 'unsafe-inline' 'unsafe-eval'` |
| `frontend/src/lib/admin-access.ts` | Documents HttpOnly migration as #1051; status/role cookies are non-credential |
| `UserController#login` | Returns JSON `token` field; no `Set-Cookie` |
| `JwtAuthenticationFilter` | Only `Authorization: Bearer …` |
| `SecurityAndCorsConfig` | CSRF disabled (Bearer-era posture); TS-0044 documents that |
| Related shipped | #1052 admin role cookie UX; #1053 session 401 → login |
| Related open | #676 refresh/revocation (do not absorb) |

Serialize: implement only after **#1048 → #1047 → #1044** clears and no
Playwright-heavy PR is in flight (prep runs while PR #1150 / #1057 CI finishes).

## Goals / Non-Goals

**Goals:**

- Browser session JWT only in HttpOnly (+ Secure + SameSite) cookie.
- Next proxy forwards cookie credentials to backend; login/logout set/clear cookie.
- Backend authenticates browser requests from cookie without requiring JS to read JWT.
- Production CSP: no `unsafe-eval`; nonce-based script policy (no blanket script
  `unsafe-inline` in production).
- Login/logout Playwright (and auth-dependent E2E helpers) stay green.

**Non-Goals:**

- #676 refresh / revocation / shorter access TTL.
- Removing Bearer support for Bruno, OpenAPI tests, or non-browser clients.
- Full CSRF token framework unless SameSite + same-origin proxy is insufficient
  (decide in Decisions; prefer KIS).
- Hardening every CSP directive beyond AC (e.g. style-src may keep `unsafe-inline`
  if Next requires it — document).

## Decisions

1. **Dual credential acceptance on the API: Cookie OR Bearer**
   - Why: AC requires cookie for the browser; Bruno/OpenAPI/integration suites
     already send Bearer. Dual read avoids a mass rewrite of API collections.
   - Alternative rejected: cookie-only — breaks tooling and raises change blast radius.

2. **Cookie attributes: `HttpOnly`, `Secure` (prod), `SameSite=Lax`, `Path=/`**
   - Why: Matches AC; Lax covers same-site top-level navigations and blocks most
     cross-site POSTs from sending the cookie; Secure required in production HTTPS.
   - Local HTTP: allow `Secure` off via profile/env (e.g. `COOKIE_SECURE=false` in
     `.env.example`) so Docker/dev login still works.
   - Cookie name: e.g. `notaire-auth-token` (final name locked at implement; must
     not collide with `notaire-auth-status` / `notaire-auth-role`).

3. **Prefer Next rewrite + cookie forward; escalate to Route Handler BFF if Set-Cookie breaks**
   - Why: App already proxies `/api/v1/:path*` via `rewrites()` to `BACKEND_URL`.
     Browser same-origin calls with `credentials: 'include'` should forward Cookie;
     if `Set-Cookie` Path/Domain rewriting fails through the rewrite, introduce a
     thin App Router route handler that proxies and normalizes `Set-Cookie`.
   - Alternative rejected: browser → backend cross-origin with CORS credentials —
     fights the existing Docker hostname isolation design.

4. **Stop persisting JWT in Zustand `persist`; keep user profile client state**
   - Why: AC forbids script-readable token. Persist `user` / `isAuthenticated`
     (and continue setting #1052 UX cookies). Never `partialize` `token` into
     `localStorage`.
   - Login JSON: prefer omitting `token` from browser-consumed response once cookie
     is set; during dual window, frontend MUST ignore any JSON token for storage.

5. **Logout MUST clear the HttpOnly cookie via an HTTP response**
   - Why: JS cannot clear HttpOnly cookies. Add/extend logout endpoint (or login
     counterpart) that `Max-Age=0`s the auth cookie; frontend logout calls it then
     clears client state + UX cookies.
   - Alternative rejected: client-only logout — leaves a valid stolen cookie until TTL.

6. **CSRF: keep CSRF disabled initially; rely on SameSite=Lax + same-origin proxy + CORS allowlist**
   - Why: KIS; browser only talks to Next origin; cross-site navigations get Lax
     restrictions; CORS already blocks disallowed origins (#691 / TS-0044).
   - Revisit: if threat model requires double-submit / Spring CSRF, open a follow-up;
     do not silently leave TS-0044 claiming “no auth cookie”.
   - Update TS-0044 to assert HttpOnly auth cookie exists and that cross-origin
     credentialed access remains blocked.

7. **Production CSP via nonce (middleware → request headers → Next scripts)**
   - Why: AC requires nonce-based CSP and removal of `unsafe-eval`. Next App Router
     supports forwarding a nonce to `<Script>` / framework scripts.
   - Dev: may keep `'unsafe-eval'` only when `NODE_ENV !== 'production'` for HMR;
     production headers MUST NOT include it.
   - `style-src 'unsafe-inline'` may remain if required by the stack; out of AC
     unless easily tightened without breakage.

8. **TDD order**
   - Backend: failing tests for Set-Cookie on login, clear on logout, filter accepts
     cookie without Bearer.
   - Frontend unit: auth-store does not persist token; api-client uses credentials /
     no localStorage bearer.
   - CSP unit/config: production header string has nonce directive and no
     `unsafe-eval`.
   - E2E last: login/logout + rewrite TS-0044 / helpers.

## Riesgos / Trade-offs

- **[Risk] Next rewrite drops or mis-scopes `Set-Cookie`** → Validate early with
  integration/E2E; fall back to Route Handler BFF (Decision 3).
- **[Risk] Ambient cookie reintroduces CSRF class** → SameSite=Lax + same-origin
  proxy + strict CORS; update TS-0044; document residual risk; follow-up if needed.
- **[Risk] Playwright helpers inject localStorage JWT** → Rewrite
  `tests/e2e/setup/*` to obtain real HttpOnly cookie (UI login or API login +
  storage state).
- **[Risk] Nonce CSP breaks Next inline bootstrap** → Use Next’s supported nonce
  plumbing; verify production build + Playwright smoke before merge.
- **[Risk] Dual Playwright-heavy PRs** → Serialize after #1048→#1047→#1044 and
  when no other Playwright-heavy PR is open (coordinator gate).
- **[Trade-off] Bearer still valid if stolen from non-browser channel** → Acceptable;
  browser XSS can no longer read the cookie; #676 covers revocation later.
- **[Trade-off] UX cookies remain forgeable** → Unchanged from #1052; API auth is
  the real control.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Login sets HttpOnly Secure SameSite cookie | integration / unit | `UserController` / security integration test |
| Logout clears auth cookie | integration | logout endpoint test |
| API accepts cookie without Bearer | unit / integration | `JwtAuthenticationFilter` (+ MockMvc/WebTestClient) |
| API still accepts Bearer | unit / integration | existing auth tests stay green |
| JWT absent from localStorage after login | unit / E2E | `auth-store` unit + Playwright login |
| Browser API call authenticates via proxy+cookie | E2E / unit | api-client credentials + E2E authenticated fetch |
| Production CSP has no `unsafe-eval` | unit / config | next config / middleware header assert |
| Production CSP script-src is nonce-based | unit / config | header contains `nonce-` / `strict-dynamic` per chosen policy |
| E2E login/logout green | E2E | TS-0002 (+ login specs); helpers updated |
| Cross-origin cannot use ambient cookie | E2E | updated TS-0044 |

- New unit tests (`src/test/java/.../unit/`): filter cookie extraction; cookie
  attribute builder.
- New integration tests (`.../integration/`): login Set-Cookie; authenticated
  request with cookie only.
- Frontend Vitest: auth-store partialize; api-client no localStorage token.
- Coverage impact (JaCoCo): small backend delta; keep ratchet; target 80% on touched
  classes.

## Regression Strategy

- Existing tests affected:
  - Frontend: `api-client.test.ts` (Bearer-from-localStorage expectations),
    `auth-store` tests, session-expiry (may still clear client state; cookie clear
    via logout API), admin-access cookie tests (UX cookies remain).
  - E2E: `setup/auth.ts`, `global-setup.ts`, TS-0002, TS-0044, TS-0093,
    any spec that `localStorage.setItem("notaire-auth", …)`.
  - Backend: login response shape tests if they require JSON `token` for browser —
    keep Bearer issuance for API clients or assert cookie + optional body.
- Full suite command: `mvn verify -pl backend-api`; `cd frontend && npm test && npm run lint`.
- HTTP/Bruno API suite: keep Bearer; should remain green without cookie.
- Legacy paths: none (`frontend-swing` removed).

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`:
  - Update login/logout (TS-0002) for HttpOnly cookie + empty/absent `notaire-auth` token.
  - Rewrite TS-0044 CSRF posture for cookie era (HttpOnly present; cross-origin still blocked).
  - Update TS-0093 session expiry if it mutates localStorage JWT.
  - Fix shared auth setup to establish cookie session.
- Golden path: login → dashboard authenticated call → logout → cookie cleared →
  protected route redirects to login.
- Edge: 401 session expiry still routes to `/login?expired=1` (#1053) and clears cookie.
- Viewports: existing suite defaults; no new marketing layout.
- Command: `cd frontend && npx playwright test` (PR heavy gate mandatory).

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: backend cookie support MUST deploy with or before
  frontend that stops sending Bearer from localStorage. Prefer single PR spanning
  both modules so `main` never strands the browser without a credential channel.
- Configuration or `.env` keys: document `COOKIE_SECURE` (or equivalent) in
  `.env.example`; no secrets in git.
- Feature flag: no (avoid dual long-lived modes); short dual-read on API only.
- Smoke test after deploy (Gate 5): UI login; DevTools Application → cookie
  HttpOnly; no JWT in localStorage; production response CSP without `unsafe-eval`;
  logout clears cookie.

## Rollback Strategy

- Revert safe: yes — revert the PR restores localStorage Bearer + old CSP; users
  re-login.
- Database rollback: none needed
- Data written under the new behavior after revert: none (cookies expire / ignored)
- Blast radius if rollback delayed: session establishment failures if proxy/cookie
  mismatch; mitigate by coupling backend+frontend in one PR and heavy-CI E2E.

## Migration Plan

1. Wait for serialize queue: **#1048 → #1047 → #1044** merged; no Playwright-heavy
   PR in flight (including finish/merge of #1150 / #1057).
2. Copy this draft into `openspec/changes/fix-1051-httponly-jwt-csp/`.
3. Validate with `bash scripts/validate-sdlc-plan.sh fix-1051-httponly-jwt-csp`.
4. Branch `cursor/fix-1051-httponly-jwt-csp-a383` → failing tests → implement →
   docs → PR `Closes #1051` → heavy CI → squash merge.
5. Gate 5 smoke on deployed stack; archive OpenSpec change.

## Open Questions

- Exact cookie name / `Max-Age` alignment with current JWT TTL (leave TTL as-is;
  #676 owns shortening).
- Whether production CSP uses `strict-dynamic` with nonce or nonce-only allowlist —
  choose the minimal Next-supported variant that passes production build + E2E.
