> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`LoginAttempt` is referenced by two types: the store and the Spring Data repository. The table is `login_attempts`.

## Goals / Non-Goals

**Goals:** security no longer imports `business.LoginAttempt`. The table and the lockout behavior stay.
**Non-Goals:** moving `Role`, `User`, audit, or reports.

## Decisions

1. Move the class to `adapter.out.persistence.security` and keep `@Table(name = "login_attempts")`. Spring Boot scans that package because it is under the application root.

## Riesgos / Trade-offs

- [A query that names the old package breaks] → JPQL uses the entity name `LoginAttempt`, which is unchanged.

## Testing Strategy

`LoginAttemptEntityLocationTest` fails while the class still lives in `business`, then passes in the new package and checks the table name.

## Regression Strategy

`LoginAttemptSharedStoreIntegrationTest` stays. JaCoCo floors are not lowered.

## Playwright Strategy

No UI change. n/a.

## Deployment Strategy

Ships with the backend image. No migration.

## Rollback Strategy

Revert the commit.
