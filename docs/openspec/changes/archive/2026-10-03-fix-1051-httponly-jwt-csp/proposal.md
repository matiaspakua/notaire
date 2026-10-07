# Move JWT out of localStorage and harden production CSP

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1051 |
| Use Case | **CU78** – Security, Privacy and Compliance; **CU84** – Login al sistema |
| Branch | `cursor/fix-1051-httponly-jwt-csp-69d3` |
| Gate 1 status | draft ready (internal); implement **after #1048→#1047→#1044** and no Playwright-heavy PR in flight |

## Objetivo

The Next.js client persists the bearer JWT in `localStorage` (`notaire-auth`) and
attaches it from script-readable storage on every API call. Production CSP also
allows `script-src 'unsafe-inline' 'unsafe-eval'`. There is no XSS sink today, but
any future XSS (including via a dependency) can exfiltrate a long-lived token and
the CSP would not stop script injection. Move the credential into an HttpOnly
cookie path and tighten production CSP so script execution is nonce-bound and
`unsafe-eval` is gone.

## What Changes

- Backend login sets an **HttpOnly, Secure, SameSite** session cookie carrying the
  JWT; logout clears it. JSON body MUST stop being the sole credential channel for
  the browser (token MAY be omitted from the login JSON once cookie delivery works,
  or kept only for non-browser clients during a short dual-read window).
- `JwtAuthenticationFilter` accepts a valid JWT from the cookie **or** from the
  existing `Authorization: Bearer` header (Bruno / API tooling keep working).
- Next.js `/api/v1` proxy (rewrite or BFF route) **forwards** the cookie to the
  backend and surfaces `Set-Cookie` from login/logout to the browser.
- Frontend removes JWT from Zustand `persist` / `localStorage`; `api-client`
  stops reading the bearer from `localStorage` for browser calls (uses
  `credentials: 'include'` / cookie ambient auth through the same-origin proxy).
- Keep non-credential UI markers (`notaire-auth-status`, `notaire-auth-role`) for
  edge middleware (#1052) unless a better cookie-derived signal is introduced in
  the same change without weakening admin UX.
- Production CSP: remove `unsafe-eval`; adopt **nonce-based** `script-src` (no
  blanket `unsafe-inline` for scripts in production). Dev may keep looser CSP if
  Next HMR requires it — document the split.
- Update Playwright / unit tests that assume localStorage JWT or “no HttpOnly
  auth cookie” (notably TS-0002, TS-0044, TS-0093, E2E auth helpers).
- **BREAKING** for any browser client that relied on reading the JWT from
  `localStorage` or on attaching `Authorization` from script-readable storage.
  Server-to-server / Bruno Bearer clients remain supported.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Session credential MUST NOT be readable by page JavaScript (HttpOnly cookie) | CU78; #1051 AC | New / Made explicit |
| Login MUST establish an authenticated session over HTTPS-ready Secure cookies | CU84; CU78 | Changed (transport of token) |
| Browser API calls via the Next proxy MUST authenticate without a script-readable bearer | CU84; #1051 AC | New |
| Production CSP MUST NOT allow `unsafe-eval`; scripts MUST be nonce-constrained | CU78; #1051 AC | New |
| Login / logout E2E golden paths MUST remain green | CU84; #1051 AC | Unchanged UX, new transport |

## Capabilities

### New Capabilities

- `httponly-jwt-cookie`: JWT session delivered and cleared via HttpOnly cookie;
  backend accepts cookie or Bearer; Next proxy forwards cookies.
- `frontend-csp-hardening`: Production CSP drops `unsafe-eval` and uses
  nonce-based `script-src` instead of blanket script `unsafe-inline`.

### Modified Capabilities

- (none under `openspec/specs/` today cover browser JWT storage or frontend CSP)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | login/logout Set-Cookie; JwtAuthenticationFilter cookie read; CSRF posture review |
| `frontend` | yes | auth-store, api-client, login/logout, CSP headers/middleware, E2E helpers/specs |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | maybe | cookie / CSP notes in security docs if present |
| CI/CD (`.github/workflows`) | no | unless a CSP/header smoke check is added (optional) |
| Scripts | maybe | preflight docs if CSP/auth smoke is documented |

### Surface area

- Entities: none
- Endpoints: `POST /api/v1/usuarios/login` (Set-Cookie); logout clear-cookie path
  (existing logout UX may call API or client-only clear — design chooses one clear
  path that invalidates the HttpOnly cookie). Bearer still accepted on `/api/**`.
- Database (Flyway `V{n}`): none (refresh/revocation remains #676)
- Configuration / `.env`: optional cookie name, `Secure` toggle for local HTTP,
  CSRF settings if enabled
- Dependencies: none required; Next nonce wiring may use middleware headers

### Architecture review

Stops treating the browser as a Bearer-header client that owns the JWT in
`localStorage`. Aligns with same-origin BFF/proxy pattern already used for
`BACKEND_URL` rewrites. Companion role/status cookies from #1052 stay as UX
signals; real API auth becomes cookie (browser) or Bearer (tools). CSRF policy
must be revisited once cookies are ambient — SameSite + same-origin proxy is the
baseline; Spring CSRF only if design proves SameSite insufficient. Related but
out of scope: #676 token refresh/revocation.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Session credential in HttpOnly cookie; CSP nonce / no `unsafe-eval` in prod |
| `docs/100-business/102-use-cases/CU84 - Login.md` | Login establishes HttpOnly cookie session; logout clears it; no localStorage JWT |
| Security / frontend architecture notes (if present under `docs/200-architecture/`) | Cookie auth + CSP production posture |
| `CHANGELOG.md` | `[Unreleased]` security entry for HttpOnly JWT + CSP hardening |

## Out of Scope

- JWT refresh tokens, short TTL access tokens, server-side revocation (#676).
- Replacing backend RBAC (#559) or removing #1052 role/status UX cookies unless
  required for cookie-auth consistency.
- Full WCAG / unrelated CSP directives beyond script hardening in AC.
- Implementing / branching / PR before the serialize queue clears.
