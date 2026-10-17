> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after item #1904 merged (R2=3 on main @ 78933df5).

## Goals / Non-Goals

**Goals:** Clear ~2 user R2 edges without behaviour change.
**Non-Goals:** Property R2; R6; FE changes; API contract changes.

## Decisions

`UserCatalogPort` / `RoleCatalogPort` already own the domain path. Entity-speaking `UserRepositoryPort` / `RoleRepositoryPort` and their PersistenceAdapters have no injectors left — delete them.

## Riesgos / Trade-offs

None material — dead code removal.

## Testing Strategy

HexagonArchitectureTest baseline shrink; existing user/role catalog and auth use-case tests.

## Regression Strategy

Existing user/role controller and Bruno coverage.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
