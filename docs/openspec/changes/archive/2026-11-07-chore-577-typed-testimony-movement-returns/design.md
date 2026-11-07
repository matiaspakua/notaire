> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#577 TestimonyMovement uses ResponseEntity<Object> for create/update/delete.

## Goals / Non-Goals

**Goals:** ['Type create/update/delete', 'Shrink baseline by 3']
**Non-Goals:** ['Closing #577']

## Decisions

['Reuse testimony domain exceptions in TestimonyMovementExceptionHandler — identical HTTP contracts']

## Riesgos / Trade-offs

['MockMvc unit tests must register TestimonyMovementExceptionHandler']

## Testing Strategy

['ControllerSignatureArchitectureTest', 'SimpleControllersTest.TestimonyMovementControllerTests', 'TestimonyPersonConstraintErrorsTest']

## Regression Strategy

['SimpleControllersTest', 'TestimonyPersonConstraintErrorsTest']

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
