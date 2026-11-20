> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`RevokedToken` is referenced by the revocation service, its unit test, and the Spring Data repository. The table is `revoked_tokens`.

## Goals / Non-Goals

**Goals:** config no longer imports `business.RevokedToken`. The table and logout behavior stay.
**Non-Goals:** moving role, user, audit, reports, or seed entities.

## Decisions

1. Move the class and the repository to `adapter.out.persistence.security`, next to the login-attempt row, and keep `@Table(name = "revoked_tokens")`.

## Riesgos / Trade-offs

- [A query that names the old package breaks] → JPQL uses the entity name `RevokedToken`, which is unchanged.

## Testing Strategy

`RevokedTokenEntityLocationTest` fails while the class still lives in `business`, then passes in the new package and checks the table name.

## Regression Strategy

`TokenRevocationServiceTest` stays, with its imports updated. JaCoCo floors are not lowered.

## Playwright Strategy

No UI change. n/a.

## Deployment Strategy

Ships with the backend image. No migration.

## Rollback Strategy

Revert the commit.
