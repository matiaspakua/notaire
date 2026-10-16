## ADDED Requirements

### Requirement: Budget template controller is ports-only
The budget-template HTTP adapter SHALL call inbound Query/Manage ports only; entity access SHALL live behind BudgetTemplateOpsPort.

#### Scenario: query via inbound port
- **WHEN** a client lists budget templates
- **THEN** the controller delegates to QueryBudgetTemplatesUseCase and returns mapped views

#### Scenario: writes via inbound port
- **WHEN** a client creates, updates, or deletes a budget template
- **THEN** the controller delegates to ManageBudgetTemplatesUseCase; validation/conflict exceptions still map via the global handler

#### Scenario: ArchUnit R1 shrinks
- **WHEN** HexagonArchitectureTest runs
- **THEN** BudgetTemplateController has no R1 baseline edges to BudgetTemplate or BudgetTemplatePK
