> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Authority is resolved per request from the stored user type (#559). The frontend mirrors the same set to hide admin routes at the edge. V2 seeds the first account as `admin` with type `Escribano`, and `DataInitializer` creates the same when the table is empty.

## Goals / Non-Goals

**Goals:** least privilege for notaries with no lock-out.
**Non-Goals:** object-level checks; permission matrix; auto-promoting unknown administrators.

## Decisions

1. **Map by user type, keep the existing column.** No schema change; the type already drives authority.
2. **`ROLE_NOTARY` is a distinct authority but needs no new endpoint rule**: notarial endpoints are open to any authenticated user today, so the split removes admin access without changing what a notary can do.
3. **Promote only the account named `admin`.** It is the one account every deployment has; any other Escribano that was an administrator is demoted and must be re-promoted deliberately, which is the point of least privilege. The migration comment and the PR say so.
4. **Order tests first**: the existing tests that mint an admin token with `Escribano` are updated to `Administrador`, because their old expectation (notary equals admin) is the defect.

## Riesgos / Trade-offs

- [An operator's second admin stored as Escribano loses admin after upgrade] → documented in the migration and PR; `admin` can promote them.
- [Frontend and backend sets drift] → both unit-tested with the same type list.

## Testing Strategy

`UserAuthorityResolverTest`, `RbacIntegrationTest` (notary 403/200), `DataInitializerTest`, `admin-access.test.ts`, `auth-store.test.ts`, and `80_roles.sql` on PostgreSQL. Written first.

## Regression Strategy

`mvn verify -pl backend-api`; Playwright and Bruno through `workspace/stack/native.sh` (the `admin` account is now an administrator).

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

`V49__promote_seeded_admin.sql`: `UPDATE users SET user_type = 'ADMIN' WHERE lower(username) = 'admin' AND lower(user_type) = 'escribano'`; idempotent.

## Open Questions

None.
