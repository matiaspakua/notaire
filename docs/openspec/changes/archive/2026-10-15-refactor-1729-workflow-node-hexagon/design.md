> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Workflow definition (#1728) done. Workflow node is the largest remaining workflow R1 hotspot.

## Goals / Non-Goals

**Goals:** ports-only WorkflowNodeController; ArchUnit shrink; REST unchanged; English identifiers.
**Non-Goals:** retiring business.WorkflowNode; migrating transition controller in this PR.

## Decisions

1. WorkflowNodeCatalogPort alongside WorkflowNodeRepositoryPort. 2. Reuse definition + management-status catalogs for lookups. 3. Preserve empty-body update 200. 4. RequiredFields stay in the HTTP adapter.

## Riesgos / Trade-offs

- [Partial update validation] → keep BusinessValidationException in controller.
- [Leak/unit tests construct with repos] → rewrite to inbound ports.

## Testing Strategy

WorkflowNodeControllerTest + use-case unit tests; ArchUnit shrink.

## Regression Strategy

mvn test -pl backend-api -Dtest=WorkflowNode*,HexagonArchitectureTest

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
