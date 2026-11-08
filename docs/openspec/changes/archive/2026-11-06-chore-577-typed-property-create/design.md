> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

#577 loose Object returns. Property update/delete already typed.

## Goals / Non-Goals

**Goals:** ['Type PropertyController#create']
**Non-Goals:** ['Closing #577']

## Decisions

['Reuse PropertyExceptionHandler']

## Riesgos / Trade-offs

['No wire change']

## Testing Strategy

['ControllerSignatureArchitectureTest after baseline shrink']

## Regression Strategy

['Existing Property controller/handler tests']

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
