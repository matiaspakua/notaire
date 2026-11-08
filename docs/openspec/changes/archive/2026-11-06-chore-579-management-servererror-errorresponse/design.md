> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Continues #579 after management-trace (#2021).

## Goals / Non-Goals

**Goals:** ['Empty 500 ServerError → throw to GlobalExceptionHandler on Management create/update', 'Unit proof']
**Non-Goals:** ['Closing #579', 'Changing DeleteResult.Conflict']

## Decisions

['Throw IllegalStateException on ServerError so GEH owns the ErrorResponse; ServerError carries no cause']

## Riesgos / Trade-offs

['Clients that only checked status remain fine; those parsing empty body now get JSON']

## Testing Strategy

['ManagementControllerServerErrorResponseTest']

## Regression Strategy

['AdditionalControllersTest ManagementController paths']

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
