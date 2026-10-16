> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after workflows; templates deferred to #1788.

## Goals / Non-Goals

**Goals:** Clear procedures/procedure-types eslint suppressions without changing REST or UI behaviour.
**Non-Goals:** Procedure-template hooks (#1788); CHANGELOG.

## Decisions

Single core/procedure-type; RequiredDocumentsDialog only switches ApiError import to lib/api-error; leave useProcedureTemplate to #1788.

## Riesgos / Trade-offs

Merge conflict with #1788/#1794 on eslint-suppressions and architecture-boundaries pins — keep shrink-only.

## Testing Strategy

Vitest: use cases + gateway + architecture pin; existing useProcedureTypes tests.

## Regression Strategy

Admin procedures Playwright flows.

## Playwright Strategy

n/a — no UI behaviour change

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
