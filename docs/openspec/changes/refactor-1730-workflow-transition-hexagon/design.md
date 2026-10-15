> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Workflow definition (#1728) and node (#1729) done. Transition is the last workflow R1 controller hotspot.

## Goals / Non-Goals

**Goals:** ports-only WorkflowTransitionController; ArchUnit shrink; REST unchanged; English identifiers.
**Non-Goals:** retiring business.WorkflowTransition; migrating users/roles in this PR.

## Decisions

1. WorkflowTransitionCatalogPort alongside WorkflowTransitionRepositoryPort. 2. Reuse definition + node catalogs for lookups. 3. RequiredFields stay in the HTTP adapter. 4. Create returns 201 with body; update returns 200 with body (preserve existing contract).

## Riesgos / Trade-offs

- [Constraint failures on save] → keep ErrorResponses.createFailed/updateFailed mapping.
- [Unit tests construct with repos] → rewrite to inbound ports.

## Testing Strategy

WorkflowTransitionControllerTest + use-case unit tests; ArchUnit shrink.

## Regression Strategy

mvn test -pl backend-api -Dtest=WorkflowTransition*,HexagonArchitectureTest

## Playwright Strategy

n/a — no UI surface change.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
