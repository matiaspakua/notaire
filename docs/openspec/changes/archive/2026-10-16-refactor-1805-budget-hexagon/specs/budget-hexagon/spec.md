## ADDED Requirements

### Requirement: Budget controller CRUD is ports-only
The budget HTTP adapter SHALL call inbound Query/Manage ports for CRUD and search; entity and PersonRepository access SHALL live behind BudgetOpsPort.

#### Scenario: query via inbound port
- **WHEN** a client lists or fetches budgets
- **THEN** the controller delegates to QueryBudgetsUseCase

#### Scenario: writes via inbound port
- **WHEN** a client creates, updates, or deletes a budget
- **THEN** the controller delegates to ManageBudgetsUseCase and maps sealed results

#### Scenario: ArchUnit R1 shrinks
- **WHEN** HexagonArchitectureTest runs
- **THEN** BudgetController has no R1 baseline edges to Budget, BudgetStatus, Person, or PersonRepository
