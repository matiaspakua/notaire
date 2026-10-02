# Frontend admin route guard for non-admin users

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1052 |
| Use Case | CU78 – Security, Privacy and Compliance; CU20/CU21 – Dar alta / Modificar usuario |
| Branch | `cursor/fix-1052_admin-route-guard-69d3` |
| Gate 1 status | pending |

## Objetivo

Admin screens under `/dashboard/administracion/**` are only hidden from the menu
via `isAdmin()`; any authenticated user can open them by URL. Middleware only
checks a forgeable presence cookie. This change adds a client layout guard and
an edge (middleware/proxy) role check so non-admins are redirected with a clear
message, fulfilling the frontend half of CU78 access control (backend RBAC remains #559).

## What Changes

- Shared admin-route / admin-tipo helpers used by middleware and UI.
- Login sets a companion `notaire-auth-role` cookie (cleared on logout) so the
  edge layer can deny `/dashboard/administracion/**` for non-admin tipos.
- `administracion` layout redirects non-admins to `/dashboard?forbidden=1`.
- Dashboard shows a short access-denied message when `forbidden=1`.
- Unit tests + Playwright TS-0094 prove non-admin cannot open admin routes.
- **Not BREAKING** for API clients; frontend UX only.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Only admin-capable roles may open administración screens | CU78 step 3 / alt 3.1; CU20/CU21 admin actors | Made explicit on the frontend |
| Unauthorized admin navigation is blocked and the user is informed | CU78 alt 3.1 (403-style denial) | New frontend UX path |

## Capabilities

### New Capabilities

- `admin-route-guard`: Edge + layout enforcement that non-admin authenticated
  users cannot reach `/dashboard/administracion/**` and are redirected with a message.

### Modified Capabilities

- (none under `openspec/specs/`)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Backend RBAC remains #559 |
| `frontend` | yes | middleware, auth cookies, admin layout, dashboard banner, tests, E2E |
| `frontend-swing` | no | Removed |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Existing frontend/Playwright jobs |

### Surface area

- Entities: none
- Endpoints: none (UI routes only)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows existing Next.js middleware + Zustand auth patterns. No ADR — not a
new architectural style; cookie role signal is a stopgap until #1051 HttpOnly auth.
Issue #1056 (middleware→proxy) is out of scope; keep `middleware.ts` as the edge layer.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Note frontend admin-route guard as UI control complementary to API RBAC |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Add TS-0094 row |
| `CHANGELOG.md` | User-visible access-denied behavior for non-admins |

## Out of Scope

- HttpOnly JWT + CSP hardening (#1051)
- Backend endpoint RBAC (#559)
- Migrating `middleware.ts` → `proxy.ts` (#1056)
- DAST / ZAP (#1067)
- Swallowing API error toasts (#1054)
