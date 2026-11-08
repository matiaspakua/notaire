> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Continues #579 after folio (#2016), property (#1999), document-type (#2000).

## Goals / Non-Goals

**Goals:** ['No catch (Exception) in ManagementStatusController', 'Unit proof unexpected create → 500 ErrorResponse']
**Non-Goals:** ['Changing ManagementStatusExceptionHandler domain mapping', 'Closing #579']

## Decisions

['Domain ManagementStatusException already propagates without the catch-all; remove both try/catch wrappers entirely']

## Riesgos / Trade-offs

['Constraint failures that ErrorResponses mapped to 400/409 must still be handled by GlobalExceptionHandler — same as prior #579 slices']

## Testing Strategy

['ManagementStatusControllerExceptionPropagationTest']

## Regression Strategy

['Existing management-status unit/integration tests']

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
