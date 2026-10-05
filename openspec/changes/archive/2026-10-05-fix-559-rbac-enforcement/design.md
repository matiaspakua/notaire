> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #559, Use Case CU78. Both API filter chains end in `anyRequest().authenticated()` and the JWT filter grants no authorities, so authorization never runs. The frontend hides administration for non-admin types (#1052) but an API client bypasses it.

## Goals / Non-Goals

**Goals:** Close the privilege escalation on user, role, audit and catalog administration; keep every existing non-admin flow working.
**Non-Goals:** A permission matrix per endpoint using `roles_permisos`; JWT refresh or revocation (#676); restricting business resources (gestiones, escrituras, pagos).

## Decisions

1. Authority is resolved from the database on each request, not embedded in the token, so a type change or deactivation takes effect immediately. Rejected: a role claim in the JWT (stale until expiry).
2. One path and method table in the API filter chain instead of annotations on 38 controllers. Rejected: `@PreAuthorize` everywhere (scattered, easy to forget on new controllers).
3. Reads of catalogs stay open: regular screens populate selectors from them. Rejected: admin-only reads (breaks gestión, presupuesto and document forms).
4. Administrator-capable types mirror the frontend set (Administrador, Admin, Escribano) in one backend class. Rejected: a new role column semantics before the roles feature is designed.

## Riesgos / Trade-offs

- One user lookup per authenticated request; acceptable for the current load and cacheable later.
- Non-admin API clients that mutate catalogs will now get 403 (documented as BREAKING for them).

- The due date is a date without time; comparisons use whole days.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Administrator-capable types | unit | `UserAuthorityResolverTest` |
| Other types | unit | `UserAuthorityResolverTest` |
| Removed or inactive user | integration | `RbacIntegrationTest` |
| Employee cannot manage users | integration | `RbacIntegrationTest` |
| Employee cannot read roles or the audit log | integration | `RbacIntegrationTest` |
| Login and logout stay public | integration | `JwtAuthIntegrationTest` |
| Employee cannot change a catalog | integration | `RbacIntegrationTest` |
| Employee can read a catalog | integration | `RbacIntegrationTest` |
| Administrator keeps full access | integration | `RbacIntegrationTest` |
| Employee opens the audit screen | e2e | `testing/e2e/tests/TS-0094-admin-route-guard.spec.ts` |

- New unit tests (`src/test/java/.../unit/`): `UserAuthorityResolverTest`, `JwtAuthenticationFilterTest` (updated), `admin-access` Vitest
- New integration tests: `RbacIntegrationTest` (H2, full security chain), Bruno `rbac/` folder
- Coverage impact: positive: new resolver and handler are covered

## Regression Strategy

- Existing tests affected: `JwtAuthenticationFilterTest` gains the resolver dependency; Bruno and E2E suites run as administrator and must stay green
- Full suite command: `mvn verify -pl backend-api; npx vitest run; Playwright full suite`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

TS-0094 gains the audit route and an API 403 case for an EMPLEADO.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit; an EMPLEADO token gets 403 on the user list

## Rollback Strategy

- Revert the PR.
