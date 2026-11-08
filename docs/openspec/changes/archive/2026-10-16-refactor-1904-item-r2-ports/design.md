> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after folio #1903 merged (R2=5 on main @ 6b868a5fd).

## Goals / Non-Goals

**Goals:** Clear ~2 item R2 edges without behaviour change.
**Non-Goals:** User/property R2; R6; FE changes; API contract changes.

## Decisions

Move ItemService beside ItemOpsAdapter; inject ItemRepository directly; delete unused entity RepositoryPort + PersistenceAdapter; BudgetCatalogItemsService / BudgetTemplateService (already outbound) inject ItemRepository and use saveAll.

## Riesgos / Trade-offs

Unit tests mock ItemRepositoryPort — switch to ItemRepository (save / saveAll / findByFkIdBudgetIdBudget).

## Testing Strategy

ItemServiceTest + BudgetCatalogItemsServiceTest + BudgetTemplateServiceTest + HexagonArchitectureTest shrink.

## Regression Strategy

Existing item OpsPort / controller coverage.

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
