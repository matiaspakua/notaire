## ADDED Requirements

### Requirement: Workflow orchestration is ports-only at the application boundary

Management HTTP adapters that expose workflow trace and document reentry SHALL call inbound ports only (`QueryWorkflowTraceUseCase`, `DocumentReentryUseCase`). Entity-bound workflow helpers SHALL live under `adapter.out.persistence.workflow`. Application outbound ports SHALL NOT expose `business.*` workflow entities. Dead entity-speaking `Workflow*RepositoryPort` types SHALL be removed.

#### Scenario: Application workflow R2 edges are gone
- **WHEN** ArchUnit R2 runs after this slice
- **THEN** no baseline edges originate from `Workflow*RepositoryPort` or `application.usecase.workflow` Trace/Reentry helpers

#### Scenario: Inbound adapters do not import outbound workflow helpers
- **WHEN** ArchUnit R5 runs after this slice
- **THEN** `ManagementController` has no edges to relocated outbound workflow helpers

#### Scenario: Trace and reentry REST behaviour unchanged
- **WHEN** clients call management workflow-trace and document-reentry endpoints
- **THEN** status codes and payloads match the pre-slice behaviour
