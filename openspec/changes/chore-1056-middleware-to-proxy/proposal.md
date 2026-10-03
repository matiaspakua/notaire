# Migrate Next.js middleware.ts to proxy.ts (Next 16)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1056 |
| Use Case | **CU84** – Login al sistema |
| Branch | `cursor/chore-1056-middleware-to-proxy-69d3` |
| Gate 1 status | draft ready (internal); implement after **#1043/#1045** queue (or coordinator promote); prefer after **#1051** if middleware gains CSP nonce; Playwright-heavy — serialize |

## Objetivo

Next.js 16.3.x deprecates the `middleware` file convention. Every `next build`
warns that `frontend/src/middleware.ts` will break when the old name is removed.
Migrate to the supported `proxy.ts` + `export function proxy` convention via the
official codemod, without changing route-guard behavior or regressing cookie auth.

## What Changes

- Apply `npx @next/codemod middleware-to-proxy` so `frontend/src/middleware.ts`
  becomes `frontend/src/proxy.ts` and the export is renamed `middleware` → `proxy`.
- Preserve existing edge behavior: public `/login`, redirect unauthenticated
  users to `/login`, bounce authenticated users off `/login` to `/dashboard`,
  deny non-admin roles on `/dashboard/administracion/**` via
  `AUTH_STATUS_COOKIE` / `AUTH_ROLE_COOKIE` helpers (#1052).
- Keep skipping `/_next`, static assets, and `/api/**` so the Next rewrite
  BFF (`next.config.ts` → `BACKEND_URL`) is never wrapped by auth redirects.
- Confirm `next build` emits **no** middleware-deprecation warning.
- Keep Auth E2E (login / logout / admin guard / session helpers) green.
- Docs/comments that say “edge middleware” for this file update to “edge proxy”
  where they refer to the Next convention (not the `/api/v1` rewrite proxy).
- **Not BREAKING** for API clients. Browser UX must remain identical.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Unauthenticated users MUST be redirected to `/login` for protected routes | CU84; #1052 | Unchanged (transport: `proxy` export) |
| Authenticated session marker MUST bounce `/login` → `/dashboard` | CU84 | Unchanged |
| Non-admin MUST NOT reach `/dashboard/administracion/**` via edge UX cookies | CU78/CU84; #1052 | Unchanged |
| Edge convention MUST use Next 16 `proxy.ts` / `export function proxy` | #1056 AC | New (convention only) |
| Auth E2E golden paths MUST remain green | CU84; #1056 AC | Unchanged UX |
| HttpOnly JWT cookie auth (#1051) MUST NOT regress | CU78/CU84; #1051 | Constraint — no regression |

## Capabilities

### New Capabilities

- `next-middleware-to-proxy`: Next 16 edge request interceptor lives in
  `proxy.ts` with `export function proxy`; deprecated `middleware.ts` removed;
  route-guard semantics preserved.

### Modified Capabilities

- (none under `openspec/specs/` today name the middleware file convention)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | `middleware.ts` → `proxy.ts`; export rename; comment/doc string updates; build warning gone; E2E stay green |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Playwright job already covers auth E2E |
| Scripts | no | — |

### Surface area

- Entities: none
- Endpoints: none (edge still skips `/api/**`)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none required beyond `@next/codemod` (npx one-shot)

### Architecture review

Mechanical Next 16 convention migration. Edge auth continues to read
**non-credential** UX cookies (`notaire-auth-status`, `notaire-auth-role`) and
delegates real API auth to the backend (#559) and, when #1051 lands, HttpOnly
JWT via the rewrite/BFF — **not** via reading the JWT in edge code. Naming
collision note: Next’s `proxy.ts` (edge interceptor) is distinct from
`next.config.ts` `rewrites()` API proxy to `BACKEND_URL`; do not merge those
concepts. No ADR required (convention rename, not architectural pivot).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU84 - Login.md` | If it names `middleware.ts`, point to `proxy.ts` / edge proxy |
| Frontend / security notes that cite `middleware.ts` for route guards | Rename convention references (keep UX-cookie semantics) |
| `CHANGELOG.md` | `[Unreleased]` chore: Next 16 middleware → proxy migration |

## Out of Scope

- Implementing HttpOnly JWT cookie delivery or CSP nonce (#1051) — track there;
  this change must not undo or conflict with that work.
- Changing matcher rules, public paths, admin deny logic, or cookie names.
- Replacing UX status/role cookies with reading the HttpOnly JWT at the edge
  (HttpOnly JWT is intentionally unreadable from document cookie; edge may see
  it only if design later chooses — not this issue).
- Backend RBAC (#559), Dependabot/image pins (#1045), frontend GHCR/semver (#1043).
- Product branch / implement PR during Gate 1 prep.
