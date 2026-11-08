> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#577 Procedure create/update use ResponseEntity<Object>. Adopted #2052 Write set + Risk protocol.

## Goals / Non-Goals

**Goals:** ['Type create/update', 'Shrink baseline by 2', 'Declare Write set']
**Non-Goals:** ['Closing #577', 'Retyping delete']

## Decisions

['NotFound via ProcedureExceptionHandler; ServerError remains IllegalStateException → GEH']

## Riesgos / Trade-offs

['MockMvc must register ProcedureExceptionHandler for 404 paths']

## Testing Strategy

['ControllerSignatureArchitectureTest', 'SimpleControllersTest.ProcedureControllerTests']

## Regression Strategy

['SimpleControllersTest.ProcedureControllerTests — PRESERVE ServerError → 500 via GEH']

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
