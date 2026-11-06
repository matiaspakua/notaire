> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Continues #579 after workflow (#2019).

## Goals / Non-Goals

**Goals:** ['No catch (Exception) on User create/update/delete', 'Unit proof unexpected create → 500 ErrorResponse']
**Non-Goals:** ['Closing #579', 'Changing login failure shape']

## Decisions

['Keep login catch that maps unexpected errors to invalid-login response (anti-enumeration)', 'Remove create/update/delete catch-alls so GEH owns unexpected failures']

## Riesgos / Trade-offs

['Delete conflict empty 409 becomes GEH-shaped 500 unless a specific conflict exception exists — same as prior #579 slices']

## Testing Strategy

['UserControllerExceptionPropagationTest']

## Regression Strategy

['Existing UserController unit/integration tests']

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
