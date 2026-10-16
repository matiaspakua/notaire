## ADDED Requirements

### Requirement: Management orchestration is ports-only at the application boundary

Management HTTP adapters SHALL call inbound ports only (`QueryManagementsUseCase`, `ManageManagementsUseCase`, `TransitionManagementUseCase`). Entity-bound management helpers SHALL live under `adapter.out.persistence.management`. Application outbound ports SHALL NOT expose `business.*` entities.

#### Scenario: Application management R2 edges are gone
- **WHEN** ArchUnit R2 runs after this slice
- **THEN** no baseline edges originate from management CU83 entity ports or `application.usecase.management` entity-bound helpers

#### Scenario: Inbound adapters do not import outbound management helpers
- **WHEN** ArchUnit R5 runs after this slice
- **THEN** `ManagementController` and `ManagementCaseSummaryController` have no edges to relocated outbound management helpers

#### Scenario: Management REST behaviour unchanged
- **WHEN** clients call `/api/v1/managements/*` (including case-summary, financial summary, pending balance, transition)
- **THEN** status codes and payloads match the pre-slice behaviour
