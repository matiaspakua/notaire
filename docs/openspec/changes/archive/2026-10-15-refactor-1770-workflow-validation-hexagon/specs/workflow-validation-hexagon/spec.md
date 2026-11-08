## ADDED Requirements

### Requirement: Workflow validation is ports-only

`WorkflowValidationController` MUST call an inbound validate port only. The use case MUST load the workflow graph through workflow catalog outbound ports and validate domain node/transition types. REST `POST /api/v1/workflow-definition/{id}/validate` MUST keep the same status codes and JSON (`valid`, `errors`). ArchUnit R1 MUST clear the controller's repository baseline edges.

#### Scenario: missing workflow returns 404

- **WHEN** a client validates a workflow id that does not exist
- **THEN** the API answers 404

#### Scenario: consistent workflow returns valid true

- **WHEN** a client validates a consistent workflow
- **THEN** the API answers 200 with `valid=true` and empty errors

#### Scenario: inconsistent workflow returns errors

- **WHEN** a client validates an inconsistent workflow
- **THEN** the API answers 200 with `valid=false` and error messages

#### Scenario: controller has no repository imports

- **WHEN** ArchUnit R1 runs after the slice
- **THEN** WorkflowValidationController has no baseline edges to Spring Data repositories
