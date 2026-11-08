> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after ProcedureType.

## Goals / Non-Goals

**Goals:** Clear one R1 edge without REST or behaviour change.
**Non-Goals:** ProcedureTemplate/Procedure hexagons; CHANGELOG.

## Decisions

Adapter wraps existing ProcedureFolderService; DTOs leave the adapter boundary.

## Riesgos / Trade-offs

Low — thin CU85 surface.

## Testing Strategy

ProcedureFolderControllerTest updated to port mocks; ArchUnit baseline shrink.

## Regression Strategy

Existing CU85 unit tests.

## Playwright Strategy

n/a — no UI surface

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
