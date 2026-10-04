> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1055 (audit-2026-09, FRONTEND, DEVOPS, security, priority:medium, CU78):
backend URL baked at build + shown on public login. Verified against
`origin/main` tip `16841b53` (2026-10-03); #1051 HttpOnly JWT/CSP is **merged**
(`c2c34de8` / PR #1156).

| Location | Finding |
|----------|---------|
| `frontend/next.config.ts` | `BACKEND_URL` captured at module load into `rewrites()` destination |
| `frontend/Dockerfile` | `ARG/ENV NEXT_PUBLIC_API_URL` at **build**; no runtime `BACKEND_URL` in image stage beyond compose env |
| `docker-compose.yml` frontend | build arg + runtime `NEXT_PUBLIC_API_URL`/`BACKEND_URL` = `http://backend:8080/api/v1` |
| `login/page.tsx` | Prints `Backend: {NEXT_PUBLIC_API_URL ?? localhost…}` to anonymous visitors |
| `api-client.ts` | Relative `BASE_URL = "/api/v1"` + `credentials: "include"` (#1051) |
| Edge file | Still `middleware.ts` (CSP nonce + UX cookies); skips `/api/`; #1056 will rename to `proxy.ts` |
| ADR-005 | Already endorses Route Handlers as light BFF |

**Why compose `BACKEND_URL` is ineffective today:** with `output: "standalone"`,
Next evaluates `rewrites()` during `next build`. The destination string is fixed
in the build output; changing the container env at `docker compose up` does not
retarget `/api/v1`. Separately, `NEXT_PUBLIC_*` values are inlined into the
client bundle, which is why login can show `http://backend:8080/api/v1`.

**#1051 non-regression (mandatory):** Browser auth uses HttpOnly
`notaire-auth-token` via same-origin BFF. The new request-time proxy MUST
forward inbound `Cookie` and outbound `Set-Cookie` (including login/logout),
keep relative `/api/v1` from the browser, and MUST NOT put JWT into
`localStorage` or any script-readable store. Edge continues to use
non-credential UX cookies (`notaire-auth-status` / `notaire-auth-role`) and skip
`/api/**`.

**#1056 coordination:** Prefer implement **after #1056** so the edge rename is
done first; this change touches the API BFF and login page, not the edge file
export name. If coordinator schedules #1055 earlier, keep `/api/**` skip intact
whether the edge file is still `middleware.ts` or already `proxy.ts`. Do not
merge concepts: edge `proxy.ts` ≠ API BFF proxy.

## Goals / Non-Goals

**Goals:**

- One frontend image: upstream API base URL chosen at **request time** from
  server env (`BACKEND_URL`).
- No internal/backend URL string rendered on `/login` (or other public pages
  introduced by this change).
- Cookie session path (#1051) remains green (unit + Playwright auth).
- Compose/docs/Dockerfile stop implying that runtime `BACKEND_URL` alone can
  retarget build-time rewrites, or that `NEXT_PUBLIC_API_URL` should carry
  Docker-internal hosts.

**Non-Goals:**

- Implementing #1056 codemod.
- Changing CSP nonce generation, edge admin deny, or backend cookie APIs.
- Publishing `BACKEND_URL` to the browser for “debug” UI.
- Replacing nginx/reverse-proxy topology from #1044 prod compose.

## Decisions

1. **Use an App Router catch-all Route Handler as the request-time BFF**
   - Path: `frontend/src/app/api/v1/[...path]/route.ts` (Node runtime).
   - Why: Matches AC (“route handler / proxy”), reads `process.env.BACKEND_URL`
     at request time in the standalone Node server, and aligns with ADR-005.
   - Remove (or no-op) the `rewrites()` destination that bakes `BACKEND_URL` at
     build; do not leave two competing proxies for the same path.
   - Alternative rejected: “hope Next runtime-evaluates rewrites” — contradicted
     by standalone bake + this audit finding.
   - Alternative rejected: edge middleware rewrite to backend — couples auth edge
     to network hop, risks #1056 collision, and edge env/networking is a worse
     fit for Docker DNS + cookie-heavy proxying.

2. **Server-only env contract**
   - Required at runtime: `BACKEND_URL` (e.g. `http://backend:8080/api/v1` in
     compose; `http://localhost:8080/api/v1` for local Node).
   - Stop passing Docker-internal hosts as `NEXT_PUBLIC_API_URL` build args.
   - If a public base URL is still needed for non-proxy cases, it must not be
     the internal hostname and must not be printed on login.
   - Document in `.env.example` + deployment docs.

3. **Cookie / header forwarding rules for #1051**
   - Forward browser `Cookie` to upstream.
   - Relay upstream `Set-Cookie` to the browser without dropping HttpOnly /
     SameSite / Path attributes required for session.
   - Forward standard hop headers carefully (`host` rewritten to upstream;
     preserve `authorization` only if present — browser sessions rely on cookie).
   - Support GET/POST/PUT/PATCH/DELETE (and HEAD/OPTIONS as needed) with body
     streaming or buffered copy appropriate to Next Route Handlers.
   - Do not log cookie values or JWT contents.

4. **Login page: delete the leak, do not replace with another URL display**
   - Remove the “Backend: …” paragraph entirely (keep “Infraestructura Segura”
     branding copy if product wants, without any host/URL).
   - Add Playwright assertion that `/login` HTML/text does not contain
     `backend:8080` or `NEXT_PUBLIC`-style internal API URLs.

5. **Implement order vs #1056 + Playwright serialize**
   - Default: **after #1056** merges (edge rename settled).
   - Always serialize against in-flight login/auth Playwright PRs (heavy-CI).
   - #1051 already on main — rebase onto updated main including cookie/CSP work.

6. **TDD order**
   - Failing unit tests for: (a) proxy resolves destination from env at call
     time; (b) Cookie/Set-Cookie forwarded; (c) login page source contains no
     Backend URL markup / no `NEXT_PUBLIC_API_URL` interpolation for that leak.
   - Then implement Route Handler + config/env cleanup + login scrub.
   - Playwright login/auth suite last.

## Riesgos / Trade-offs

- **[Risk] Dual proxy if rewrites left in place** → Remove rewrite entries for
  `/api/v1` when Route Handler lands; verify with a single integration call.
- **[Risk] Incomplete `Set-Cookie` forwarding breaks login after #1051** →
  Explicit unit tests + Playwright login/logout; manual smoke of cookie flags.
- **[Risk] Body/streaming or multipart upload regressions** → Cover JSON login
  - at least one mutating API path used by E2E; note binary report downloads if
  proxied through `/api/v1`.
- **[Risk] Overlap with #1056 edge rename / Auth E2E** → Implement after #1056
  when possible; serialize Playwright-heavy PRs.
- **[Risk] Operators still set only `NEXT_PUBLIC_API_URL`** → Docs +
  `.env.example` + compose use `BACKEND_URL`; fail closed with clear 500/log if
  missing in production.
- **[Trade-off] Slightly more code than config rewrite** → Necessary for true
  runtime retargeting under standalone output.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Proxy uses runtime BACKEND_URL (not build bake) | unit | frontend unit for proxy helper / route handler with mocked `fetch` + env |
| Missing BACKEND_URL fails safely (no silent wrong host) | unit | same |
| Cookie request header forwarded upstream | unit | proxy helper tests |
| Set-Cookie from upstream relayed to browser | unit | proxy helper tests |
| Login page does not disclose backend URL | unit + E2E | component/source assert + Playwright `/login` |
| Browser API calls stay same-origin `/api/v1` | unit / existing | `api-client` tests / grep assert |
| HttpOnly session login/logout still works | E2E | Playwright auth (TS-0002 / post-#1051 helpers) |
| Edge still skips `/api/**` (middleware or proxy.ts) | unit / existing | middleware/proxy skip + optional assert |

- New unit tests: Node-side proxy helper extracted from the Route Handler for
  pure env + header tests (SRP).
- Integration (Java): n/a
- Coverage (JaCoCo): n/a delta; Vitest on new frontend units.

## Regression Strategy

- Existing tests affected:
  - Auth store / session-expiry / api-client tests — must stay on
    `credentials: 'include'` + relative `/api/v1`.
  - Playwright auth suite — must pass after BFF swap.
  - Any test that assumed `next.config` rewrite-only (update comments).
  - Login page visual/E2E selectors — remove dependency on Backend URL text.
- Full suite command: `cd frontend && npm test && npm run lint && npm run build`
  then Playwright; `mvn test -pl backend-api` unchanged green.
- HTTP/Bruno API suite: n/a for contract (direct backend); optional smoke via
  frontend origin.
- Legacy paths: none.

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`:
  - Assert `/login` does **not** show `Backend:` / `backend:8080` /
    internal API URL strings (new or extend security/login spec).
  - Re-run auth golden paths (login → dashboard → logout → cookie session).
- Golden path: anonymous login page clean of infra URLs; authenticated API via
  same-origin proxy with HttpOnly cookie.
- Edge / error paths: wrong/missing `BACKEND_URL` surfaces as server error to
  API calls without exposing the upstream URL on `/login`.
- Viewports: existing suite defaults; login at 320/768/1024 if layout shifts.
- Command: `cd frontend && npx playwright test`
- **Serialize** if other login/auth E2E PRs are in flight; do not mark Playwright
  n/a — UI + auth path touched.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend-only image rebuild; set runtime
  `BACKEND_URL` per environment; remove obsolete internal `NEXT_PUBLIC_API_URL`
  build args from compose/Dockerfile as designed.
- Configuration or `.env` keys: `BACKEND_URL` (server); document removal/avoidance
  of internal hosts in `NEXT_PUBLIC_API_URL`; sync `.env.example`
- Feature flag: no
- Smoke test after deploy (Gate 5): open `/login` (no backend URL text); login;
  confirm `notaire-auth-token` HttpOnly cookie set; API calls succeed; retarget
  `BACKEND_URL` in a non-prod check without rebuilding image if feasible.

## Rollback Strategy

- Revert safe: yes — restore `rewrites()` + prior login markup (reintroduces bake
  - leak; acceptable emergency rollback).
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback delayed: API proxy failures (500/network) → users
  cannot login; mitigate with Playwright + smoke before promote.

## Migration Plan

1. Wait for serialize: prefer **#1056** merged; no login/auth Playwright-heavy PR
   in flight; base on updated `main` (includes #1051).
2. Copy this draft into `openspec/changes/fix-1055-backend-url-runtime/`.
3. Validate: `bash scripts/validate-sdlc-plan.sh fix-1055-backend-url-runtime`.
4. Branch `cursor/fix-1055-backend-url-runtime-2df6` from updated `main`.
5. TDD → Route Handler BFF → remove rewrite bake → scrub login → env/docs → PR
   `Closes #1055` → heavy CI → merge.
6. Gate 5 smoke; archive OpenSpec change.

## Open Questions

- None blocking Gate 1. Exact helper module path
  (`frontend/src/lib/backend-proxy.ts` vs colocated route) chosen at implement
  under SRP; behavior is fixed by the delta spec.
