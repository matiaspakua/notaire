> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Authority and module grants read `UserRepository` and the JPA user graph. Domain user and role queries already expose active, type, role id, and modules.

## Goals / Non-Goals

**Goals:** those two services stop importing `business.User` and `business.Role`. Behavior stays.
**Non-Goals:** changing audit, reports, or seed code.

## Decisions

1. Authority uses `QueryUsersUseCase.findByName` and `User.active()` / `User.type()`.
2. Grants use the same user query. When `roleId` is present, modules come from `QueryRolesUseCase.findById`. When it is absent, or the role row is gone, the type template is used.

## Riesgos / Trade-offs

- [A grant check does one extra role read] → the role catalog is small and the previous code loaded the role through the user association anyway.

## Testing Strategy

The existing unit tests are rewritten onto domain users and roles. They fail to compile against the repository constructors, then pass.

## Regression Strategy

`RoleModuleGrantFallbackIntegrationTest` stays on the Spring bean. JaCoCo floors are not lowered.

## Playwright Strategy

No UI change. n/a.

## Deployment Strategy

Ships with the backend image. No migration.

## Rollback Strategy

Revert the commit. The API shape is unchanged.
