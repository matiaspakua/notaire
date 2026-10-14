> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The client sends the version it read; the legacy controller copies it onto the entity so a stale value is rejected by the persistence layer. The new use case compares versions explicitly and reports a domain stale-version exception.

## Goals / Non-Goals

**Goals:** the third catalog on the pattern, shaped so the shared rule can be extracted afterwards.
**Non-Goals:** changing validation or response shapes.

## Decisions

1. **Explicit version check in the use case** instead of relying on the persistence layer to notice a changed version on a managed entity. 2. **Keep the DTO as the controller's wire shape** so OpenAPI is untouched.

## Riesgos / Trade-offs

- [A subtle change in the 409/400 mapping] → pinned by characterization tests before the change.
- [Optimistic locking semantics] → the stale-version test runs on both implementations.

## Testing Strategy

FolioTypeApiCharacterizationTest passes before and after; domain, use-case and adapter tests; existing folio type tests stay green.

## Regression Strategy

mvn verify -pl backend-api

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
