> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`isAdmin()` only filters sidebar/dashboard tiles. `dashboard/layout.tsx` checks
`isAuthenticated` only. `middleware.ts` gates on `notaire-auth-status` presence.
Direct navigation to `/dashboard/administracion/usuarios` or `/roles` works for
any logged-in user. Issue #1052 AC requires layout + edge (proxy/middleware)
guard, redirect with message, and Playwright proof. Frontend half of #559.

## Goals / Non-Goals

**Goals:**

- Deny `/dashboard/administracion/**` for non-admin tipos at edge + layout.
- Redirect non-admins to `/dashboard?forbidden=1` with a visible message.
- Prove with unit tests and Playwright (create EMPLEADO, attempt admin URL).

**Non-Goals:**

- HttpOnly cookies / CSP (#1051), backend RBAC (#559), middleware→proxy rename (#1056).
- Guarding every `adminOnly` tile path beyond administración (e.g. auditoria) unless
  needed for the same helper — AC scope is administración.

## Decisions

1. **Shared pure helpers in `frontend/src/lib/admin-access.ts`**
   - Why: one definition of admin tipos and admin path prefix for middleware,
     layout, and unit tests (TDD-friendly).
   - Admin tipos match existing `isAdmin()`: `ADMIN`, `ADMINISTRADOR`, `ESCRIBANO`.

2. **Companion cookie `notaire-auth-role=<tipo>` set on login / cleared on logout**
   - Why: edge middleware cannot read localStorage; AC requires proxy/middleware
     role guard. Cookie remains forgeable (same class as status cookie) — documented
     risk until #1051; layout guard still checks Zustand `user.tipo`.
   - Alternative rejected: JWT decode in middleware (no shared secret on edge today;
     larger scope).

3. **Redirect target `/dashboard?forbidden=1` + banner**
   - Why: keeps user in-app; message satisfies AC; easy Playwright assertion.
   - Alternative rejected: dedicated `/403` page (extra route, no design-system need).

4. **`administracion/layout.tsx` client guard**
   - Why: AC “layout + proxy”; catches cases where role cookie is missing/stale
     but Zustand has EMPLEADO.

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Role cookie forgeable | Document as frontend UX control; real enforcement is #559/#1051 |
| E2E auth helpers that only set status cookie break admin deep-links | Update shared auth helpers / login path to set role; UI login covers TS-0094 |
| Existing sessions without role cookie hit admin after deploy | Middleware treats missing role on admin path as deny (redirect forbidden) — users re-login |
| Overlap with #1056 proxy rename | Keep `middleware.ts`; helpers stay portable |

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Non-admin edge deny for admin path | unit | `admin-access.test.ts` (+ middleware pure path) |
| Admin edge allow for admin path | unit | `admin-access.test.ts` |
| Non-admin layout redirect | unit/component | `admin-route-guard.test.tsx` or layout test |
| Forbidden banner on dashboard | unit/component | dashboard or focused test |
| Non-admin cannot open admin routes (E2E) | E2E | `TS-0094-admin-route-guard.spec.ts` |
| Admin still reaches administración | E2E / existing TS-0023 | regression |

- New unit tests: `frontend/src/tests/unit/admin-access.test.ts` (+ guard UI as needed)
- New integration tests: n/a — frontend-only
- Coverage impact (JaCoCo): unchanged

## Regression Strategy

- Existing tests affected: `auth-store.test.ts` (logout clears role cookie);
  login/E2E auth setup may need role cookie; TS-0023 admin workflows stay green
- Full suite command: frontend `npm test` + Playwright; backend sanity optional
- HTTP/Bruno: n/a
- Legacy paths at risk: none

## Playwright Strategy

- Add `frontend/tests/e2e/TS-0094-admin-route-guard.spec.ts` (CU78, #1052)
- Golden path: admin creates EMPLEADO → login as EMPLEADO → goto
  `/dashboard/administracion/usuarios` → land on `/dashboard?forbidden=1` with message
- Edge: admin can still open administración (or rely on TS-0023)
- Viewports: assert forbidden message at 320 / 768 / 1024
- Command: `cd frontend && npx playwright test TS-0094-admin-route-guard`

## Deployment Strategy

- Flyway migration required: no
- Deployment order: frontend-only
- Configuration / `.env`: none
- Feature flag: no
- Smoke test: login as EMPLEADO (or create one), open admin URL, confirm redirect + message; login as admin, confirm admin pages work

## Rollback Strategy

- Revert safe: yes (frontend-only)
- Database rollback: none
- Data written under new behavior: none
- Blast radius if delayed: non-admins briefly regain direct URL access to admin UI (API still auth-gated)

## Migration Plan

1. Ship helpers + cookie + middleware + layout + banner + tests.
2. Users with old sessions re-login to obtain role cookie (or get denied until they do).

## Open Questions

None — AC and CU78 alt 3.1 are sufficient.
