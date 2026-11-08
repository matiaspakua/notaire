> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 after AuxiliaryProtocol #1825. Remaining R1=9 on main.

## Goals / Non-Goals

**Goals:** Clear all 9 R1 edges (ItemResponse×2, *Ref×6, ErrorResponses×1).
**Non-Goals:** Hexagon for BudgetTemplateService internals; FE.

## Decisions

- Map Item→ItemView inside BudgetTemplateService / BudgetCatalogItemsService (application may still use entities — R2).
- Delete unused `*Ref.from(entity)` methods (no call sites remain after controller ports-only wave).
- Detect JPA optimistic-lock by class name string to avoid `jakarta.persistence` dependency in adapter.in.

## Riesgos / Trade-offs

Method-reference overload resolution on `ItemResponse::from` must target `ItemView` only after `from(Item)` removal.

## Testing Strategy

Unit tests for ItemResponse / ErrorResponses; HexagonArchitectureTest shrink; existing budget/item integration tests.

## Regression Strategy

Bruno budget items-from-template / catalog if present; ItemController tests.

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
