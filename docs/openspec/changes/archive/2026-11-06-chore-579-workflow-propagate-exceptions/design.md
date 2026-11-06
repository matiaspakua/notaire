> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Continues #579 after submitted-document (#2018).

## Goals / Non-Goals

**Goals:** ['No catch (Exception) + ErrorResponses in the three workflow controllers', 'Unit proof unexpected definition create → 500']
**Non-Goals:** ['Closing #579', 'Moving domain 404/409 mapping into a ControllerAdvice']

## Decisions

['Keep inline catches for Workflow*NotFound/Invalid/InUse; only remove the ErrorResponses catch-all']

## Riesgos / Trade-offs

['Constraint violations now rely on GlobalExceptionHandler DataIntegrity mapping (already 400) — same as prior #579 slices']

## Testing Strategy

['WorkflowDefinitionControllerExceptionPropagationTest']

## Regression Strategy

['Existing workflow unit/integration tests']

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
