> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

ProcedureType write methods return Object for mixed success and error bodies.

## Goals / Non-Goals

**Goals:** ['Typed create/update/delete via advice']
**Non-Goals:** ['assignWorkflow tip', 'Closing #577']

## Decisions

['ProcedureTypeExceptionHandler mirrors Concept tip shapes']

## Riesgos / Trade-offs

['None']

## Testing Strategy

['ArchUnit baseline shrink; SimpleControllersTest ProcedureType nested suite with advice']

## Regression Strategy

['mvn verify']

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Next release.

## Rollback Strategy

Revert PR.

## Migration Plan

None.

## Open Questions

None.
