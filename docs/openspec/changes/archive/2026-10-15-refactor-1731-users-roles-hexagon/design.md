> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Workflow trio (#1728–#1730) done. Users+roles are security-sensitive and must not thrash #1683 RBAC enforcement.

## Goals / Non-Goals

**Goals:** ports-only UserController and RoleController; ArchUnit shrink; REST/login unchanged; English identifiers; fence #1683.
**Non-Goals:** enforcing role_modules; retiring JPA entities; FE catalog (#1732).

## Decisions

1. UserCatalogPort + RoleCatalogPort alongside legacy repository ports. 2. PasswordHasherPort wraps PasswordEncoder + legacy MD5 verify. 3. AuthenticateUserUseCase owns credential check and MD5 upgrade; JWT/cookies/metrics/lockout stay in the HTTP adapter. 4. Domain field `modules`; wire JSON remains `modulos`. 5. RequiredFields/validation stay in adapters.

## Riesgos / Trade-offs

- [Login hash tests construct with repos] → rewrite to ports/use cases.
- [Overlap with #1683] → no grant enforcement in this PR; PR notes the fence.
- [Password in domain model] → domain stores hash only; raw password never persists on the domain type after encode.

## Testing Strategy

RoleControllerTest, UserControllerHashTest, use-case unit tests; ArchUnit shrink; security gates via pipeline.

## Regression Strategy

mvn test -pl backend-api -Dtest=RoleController*,UserController*,User*Hash*,HexagonArchitectureTest

## Playwright Strategy

n/a — no UI surface change.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
