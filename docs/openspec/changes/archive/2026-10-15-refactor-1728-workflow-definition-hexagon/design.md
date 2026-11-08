> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Folio instance (#1727) done. Workflow definition is the next P1 repo-only controller.

## Goals / Non-Goals

**Goals:** ports-only WorkflowDefinitionController; ArchUnit shrink; REST unchanged; English identifiers.
**Non-Goals:** retiring business.WorkflowDefinition; migrating node/transition controllers in this PR.

## Decisions

1. WorkflowDefinitionCatalogPort alongside WorkflowDefinitionRepositoryPort. 2. hasNodes on catalog for delete guard. 3. Preserve empty-body update 200. 4. CHANGELOG under Changed, not Features.

## Riesgos / Trade-offs

- [Update empty body] → keep ResponseEntity.ok().build().
- [Leak test constructs controller with repos] → rewrite to inbound ports.

## Testing Strategy

WorkflowDefinitionControllerTest + use-case unit tests; ControllerExceptionMessageLeakTest; ArchUnit shrink.

## Regression Strategy

mvn test -pl backend-api -Dtest=WorkflowDefinition*,ControllerExceptionMessageLeakTest,HexagonArchitectureTest

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
