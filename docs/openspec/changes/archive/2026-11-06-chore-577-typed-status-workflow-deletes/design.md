> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#577 Object returns. These two deletes never return a body.

## Goals / Non-Goals

**Goals:** ['Type two Void deletes', 'Shrink baseline by 2']
**Non-Goals:** ['Map-body conflict deletes']

## Decisions

['Reuse existing exception handlers / try-catch shapes']

## Riesgos / Trade-offs

['No wire change']

## Testing Strategy

['ControllerSignatureArchitectureTest']

## Regression Strategy

['Existing controller tests']

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
