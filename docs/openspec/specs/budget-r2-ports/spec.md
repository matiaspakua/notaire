# budget-r2-ports Specification

## Purpose
TBD - created by archiving change refactor-1865-budget-r2-ports. Update Purpose after archive.

## Requirements

### Requirement: Budget orchestration is ports-only at the application boundary

Budget HTTP adapters SHALL call inbound ports only (`QueryBudgetsUseCase`, `ManageBudgetsUseCase`, `LoadBudgetItemsFromTemplateUseCase`, `AddBudgetItemsFromCatalogUseCase`). Entity-bound budget helpers SHALL live under `adapter.out.persistence.budget`. Application outbound ports SHALL NOT expose `business.Budget` or Spring Data paging types. Entity-speaking `BudgetRepositoryPort` SHALL be removed.

#### Scenario: Application budget R2 edges are gone
- **WHEN** ArchUnit R2 runs after this slice
- **THEN** no baseline edges originate from `BudgetRepositoryPort` or `application.usecase.budget` entity-bound helpers

#### Scenario: Inbound adapters do not import outbound budget helpers
- **WHEN** ArchUnit R5 runs after this slice
- **THEN** `BudgetController` has no edges to relocated outbound budget helpers

#### Scenario: Budget REST behaviour unchanged
- **WHEN** clients call `/api/v1/budgets/*` including CU39 template load and CU71 catalog items
- **THEN** status codes and payloads match the pre-slice behaviour
