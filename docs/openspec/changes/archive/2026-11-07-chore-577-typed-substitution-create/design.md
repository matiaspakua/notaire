> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Substitution create returns Object for mixed success and message maps.

## Goals / Non-Goals

**Goals:** ['Typed create return via advice']
**Non-Goals:** ['Closing #577']

## Decisions

['SubstitutionExceptionHandler maps BadRequest/NotFound to {"message"} bodies']

## Riesgos / Trade-offs

['None']

## Testing Strategy

['ArchUnit baseline shrink; existing NotebookControllerTest integration']

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
