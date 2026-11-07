> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#577 Testimony uses ResponseEntity<Object> for create/update/delete.

## Goals / Non-Goals

**Goals:** ['Type create/update/delete', 'Shrink baseline by 3']
**Non-Goals:** ['Closing #577', 'TestimonyMovementController']

## Decisions

['Advice maps Failed via createFailed/updateFailed', 'StaleVersion via ErrorResponses.staleVersion', 'InUse/Conflict delete → plain-text 409', 'Unexpected Updated/Created → empty 500']

## Riesgos / Trade-offs

['MockMvc unit tests must register TestimonyExceptionHandler']

## Testing Strategy

['ControllerSignatureArchitectureTest', 'SimpleControllersTest.TestimonyControllerTests', 'TestimonyPersonConstraintErrorsTest']

## Regression Strategy

['SimpleControllersTest', 'TestimonyPersonConstraintErrorsTest', 'integration testimony suites']

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
