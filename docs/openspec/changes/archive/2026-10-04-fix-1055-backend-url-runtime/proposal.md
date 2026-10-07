# Runtime backend URL proxy + remove login URL leak

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1055 |
| Use Case | **CU78** – Security, Privacy and Compliance |
| Branch | `cursor/fix-1055-backend-url-runtime-2df6` (create at implement time) |
| Gate 1 status | draft ready (internal); implement after **#1056** (or as coordinator schedules); Playwright-serialize if touching login/auth E2E |

## Objetivo

Standalone Next builds evaluate `next.config.ts` `rewrites()` at `next build`, so
runtime `BACKEND_URL` in compose cannot retarget the API proxy, and the public
login page prints an internal backend URL to anonymous visitors. Fix both so one
frontend image runs in any environment without leaking infrastructure.

## What Changes

- Replace build-time `/api/v1` rewrites with a **request-time** server proxy
  (Next.js Route Handler catch-all, or equivalent Node runtime proxy) that reads
  `BACKEND_URL` (or a documented server-only fallback) on each request.
- Remove the “Backend: …” line (and any other public display of
  `NEXT_PUBLIC_API_URL` / internal hostnames) from `login/page.tsx`.
- Stop baking internal Docker hostnames into the client via
  `NEXT_PUBLIC_API_URL` build args where they exist only for that leak / rewrite;
  keep browser API calls on relative `/api/v1` (`api-client.ts`).
- Preserve HttpOnly JWT cookie auth from #1051 (`c2c34de8` on main): forward
  `Cookie` / `Set-Cookie`, keep `credentials: 'include'`, do not reintroduce
  script-readable JWT storage.
- Stay compatible with #1056 (`middleware.ts` → `proxy.ts`): edge interceptor
  continues to skip `/api/**`; do not conflate Next edge `proxy.ts` with this
  API BFF proxy.
- **Not BREAKING** for REST clients that already call same-origin `/api/v1`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Internal backend hostnames MUST NOT be shown to anonymous visitors on `/login` | CU78; #1055 AC | New (remove leak) |
| Frontend API proxy destination MUST resolve at request time from server env so one image works across environments | CU78; #1055 AC; ADR-005 BFF | Changed (build-time rewrite → runtime Route Handler / proxy) |
| Browser sessions MUST continue to authenticate via HttpOnly cookie through the same-origin BFF | CU78/CU84; #1051 | Constraint — no regression |
| Edge auth interceptor MUST keep skipping `/api/**` (middleware today; `proxy.ts` after #1056) | CU84; #1052/#1056 | Unchanged interaction |
| Secrets / internal URLs stay server-only (`.env` / compose), never hardcoded in client bundles for infra display | Constitution P9; CU78 | Made explicit |

## Capabilities

### New Capabilities

- `runtime-backend-url-proxy`: Request-time BFF proxy for `/api/v1/**` driven by
  runtime `BACKEND_URL`, plus removal of public backend URL disclosure on login.

### Modified Capabilities

- (none under `openspec/specs/` today name the Next rewrite / login URL leak)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Cookie auth contract unchanged (#1051 already on main) |
| `frontend` | yes | Remove build-time rewrite destination; add Route Handler (or equivalent) proxy; scrub login page; Dockerfile/compose/env docs for server-only `BACKEND_URL` |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | maybe | Only if compose/docs mention `NEXT_PUBLIC_API_URL` for frontend internal URL |
| CI/CD (`.github/workflows`) | maybe | Build args that pass internal `NEXT_PUBLIC_API_URL` into frontend image |
| Scripts | maybe | Start/docs scripts that document the env vars |

### Surface area

- Entities: none
- Endpoints: same-origin browser path `/api/v1/**` unchanged; server-side
  upstream target becomes runtime `BACKEND_URL`
- Database (Flyway `V{n}`): none
- Configuration / `.env`: document `BACKEND_URL` as **runtime server-only**;
  stop using `NEXT_PUBLIC_API_URL` for Docker-internal hostnames; add/adjust
  `.env.example` keys as needed (no secrets committed)
- Dependencies: none required beyond Next.js App Router Route Handlers

### Architecture review

Aligns with ADR-005 (Next Route Handlers as light BFF) and with #1051’s
same-origin cookie path. Prefer Node runtime Route Handler over edge rewrite so
standalone containers read `BACKEND_URL` at process start / request time.
Naming collision with #1056: Next `proxy.ts` = edge interceptor; this change’s
“proxy” = API BFF under `/api/v1`. No new ADR required if we follow ADR-005;
cite it in permanent docs updates.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Note that public UI must not disclose internal backend URLs; cite #1055 |
| Frontend / deployment docs that describe `NEXT_PUBLIC_API_URL` + `rewrites()` | Document runtime `BACKEND_URL` + Route Handler BFF; one-image multi-env |
| `docs/200-architecture/209-deployment/README.md` (or compose docs) | Clarify frontend env: runtime `BACKEND_URL`, no client-baked internal host |
| `CHANGELOG.md` | `[Unreleased]` security/fix: runtime backend URL + login leak removed |

## Out of Scope

- Implementing #1056 middleware→proxy (OpenSpec ready; schedule first or
  coordinator-ordered).
- Re-opening #1051 (already merged `c2c34de8`) except non-regression constraints.
- Changing JWT cookie names, CSP nonce logic, or edge route-guard rules.
- Exposing a new public config endpoint that returns `BACKEND_URL` to the browser.
- Kubernetes / external API gateway redesign beyond compose/Docker env wiring.
- Product branch / implement PR during Gate 1 prep.
