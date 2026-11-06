> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Continues #579 after user (#2020).

## Goals / Non-Goals

**Goals:** ['No catch (Exception) on getWorkflowTrace', 'Unit proof unexpected buildTrace → 500 ErrorResponse']
**Non-Goals:** ['Closing #579', 'Changing IllegalArgumentException 400 body']

## Decisions

['Keep IllegalArgumentException → 400 with Map error body; remove only the outer Exception catch']

## Riesgos / Trade-offs

['Empty 500 becomes GEH-shaped ErrorResponse — clients that only checked status stay fine']

## Testing Strategy

['ManagementControllerExceptionPropagationTest']

## Regression Strategy

['WorkflowTrace API/integration tests']

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
