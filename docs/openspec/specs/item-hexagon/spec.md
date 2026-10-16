# item-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1809-item-hexagon. Update Purpose after archive.

## Requirements

### Requirement: Item controller is ports-only
The item HTTP adapter SHALL call inbound Query/Manage ports only; entity and BudgetRepository access SHALL live behind ItemOpsPort.

#### Scenario: query via inbound port
- **WHEN** a client lists items or filters by budget / discounts-surcharges
- **THEN** the controller delegates to QueryItemsUseCase and returns mapped views

#### Scenario: writes via inbound port
- **WHEN** a client creates, updates, or deletes an item
- **THEN** the controller delegates to ManageItemsUseCase; validation/not-found still map to 400/404

#### Scenario: ArchUnit R1 shrinks
- **WHEN** HexagonArchitectureTest runs
- **THEN** ItemController has no R1 baseline edges to Item, Budget, or BudgetRepository
