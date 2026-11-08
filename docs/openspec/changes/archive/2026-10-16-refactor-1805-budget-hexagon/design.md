> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after BudgetTemplate (#1803).

## Goals / Non-Goals

**Goals:** ['ports-only BudgetController CRUD', 'clear R1 edges']
**Non-Goals:** ['CHANGELOG', 'move template/catalog item services']

## Decisions

['OpsAdapter wraps BudgetService + PersonRepository; keep template/catalog services on controller']

## Riesgos / Trade-offs

['PaginationTest and SimpleControllersTest must mock inbound ports']

## Testing Strategy

SimpleControllersTest BudgetControllerTests; PaginationTest; HexagonArchitectureTest

## Regression Strategy

Bruno budgets; BudgetPersonAssociationPgIntegrationTest

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
