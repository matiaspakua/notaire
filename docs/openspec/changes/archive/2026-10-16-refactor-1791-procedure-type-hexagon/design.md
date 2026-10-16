> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693; first Procedure* controller.

## Goals / Non-Goals

**Goals:** Clear R1 edges without changing REST contracts.
**Non-Goals:** Procedure/Folder/Template controllers; FE; CHANGELOG.

## Decisions

OpsPort returns DTOs and sealed results; workflow assignment keeps Map body semantics for null vs missing (#655).

## Riesgos / Trade-offs

SimpleControllersTest and ProcedureTypeWorkflowAssignmentTest must mock inbound ports.

## Testing Strategy

Update unit tests; existing integration coverage.

## Regression Strategy

CoreBusinessControllersIntegrationTest ProcedureTypeController tests.

## Playwright Strategy

n/a — no UI behaviour change

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
