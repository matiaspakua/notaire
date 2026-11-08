# workflow-trace-tx-boundary Specification

## Purpose
TBD - created by archiving change fix-1745-workflow-trace-rollback. Update Purpose after archive.

## Requirements

### Requirement: Workflow-trace adapter does not own a swallowing transaction

`ManagementController.getWorkflowTrace` SHALL NOT be annotated with `@Transactional`. The read transaction SHALL remain on `WorkflowTraceService.buildTrace` so expected `IllegalArgumentException` outcomes complete the service transaction before the adapter maps them to HTTP 400.

#### Scenario: Controller method has no Transactional annotation

- **WHEN** `ManagementController.getWorkflowTrace` is inspected
- **THEN** it has no `@Transactional` annotation

#### Scenario: Missing management answers 400 without UnexpectedRollbackException

- **WHEN** a client GETs `/api/v1/managements/{id}/workflow-trace` for an unknown id
- **THEN** the response is 400 and no `UnexpectedRollbackException` is logged at ERROR
