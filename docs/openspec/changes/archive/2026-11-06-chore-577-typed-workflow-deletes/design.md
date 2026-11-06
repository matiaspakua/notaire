> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#577 Object returns on workflow deletes due to controller catch Map bodies.

## Goals / Non-Goals

**Goals:** ['Exception handlers + Void deletes', 'Shrink baseline by 2']
**Non-Goals:** ['Closing #577', 'Typing create Invalid Map paths']

## Decisions

['Dedicated @RestControllerAdvice per controller like FolioType']

## Riesgos / Trade-offs

['Wire shape unchanged']

## Testing Strategy

['Handler MockMvc tests red-then-green', 'ControllerSignatureArchitectureTest']

## Regression Strategy

['Existing WorkflowDefinitionControllerExceptionPropagationTest']

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
