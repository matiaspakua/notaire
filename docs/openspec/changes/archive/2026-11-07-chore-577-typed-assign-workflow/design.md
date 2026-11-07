> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#577 assignWorkflow uses ResponseEntity<Object>.

## Goals / Non-Goals

**Goals:** ['Type assignWorkflow', 'Shrink baseline by 1']
**Non-Goals:** ['Closing #577']

## Decisions

['Reuse ProcedureTypeInUseException for inactive workflow 409 {"error"} body']

## Riesgos / Trade-offs

['None — handler already maps InUse/NotFound']

## Testing Strategy

['ControllerSignatureArchitectureTest']

## Regression Strategy

['Existing ProcedureTypeControllerTests']

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
