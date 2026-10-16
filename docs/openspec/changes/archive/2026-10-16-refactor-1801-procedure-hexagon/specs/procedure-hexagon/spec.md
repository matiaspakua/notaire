## ADDED Requirements

### Requirement: Procedure controller is ports-only
The procedure HTTP adapter SHALL call inbound Query/Manage ports only; entity and repository access SHALL live behind ProcedureOpsPort.

#### Scenario: query via inbound port
- **WHEN** a client lists or fetches a procedure
- **THEN** the controller delegates to QueryProceduresUseCase and returns the mapped view

#### Scenario: writes via inbound port
- **WHEN** a client creates, updates, or deletes a procedure
- **THEN** the controller delegates to ManageProceduresUseCase and maps sealed results to HTTP statuses

#### Scenario: ArchUnit R1 shrinks
- **WHEN** HexagonArchitectureTest runs
- **THEN** ProcedureController has no R1 baseline edges to Procedure, Budget, or deed repositories
