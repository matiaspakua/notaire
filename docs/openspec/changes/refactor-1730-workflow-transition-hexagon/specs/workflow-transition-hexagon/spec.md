## ADDED Requirements

### Requirement: Workflow transitions are managed through domain-typed ports

The workflow-transition HTTP adapter MUST call inbound ports only. Use cases MUST validate workflow definition and node references on create/update.

#### Scenario: CRUD keeps the wire contract

- **WHEN** a client lists by workflow, gets, creates, updates, or deletes a workflow transition
- **THEN** status codes and JSON fields match the pre-refactor contract

#### Scenario: missing workflow or node on create answers 404

- **WHEN** create references an absent workflow definition, origin node, or destination node
- **THEN** the response is 404

#### Scenario: missing transition or node on update answers 404

- **WHEN** update targets an absent transition or references an absent node
- **THEN** the response is 404

#### Scenario: controller has no persistence imports

- **WHEN** ArchUnit R1 runs after the slice
- **THEN** WorkflowTransitionController has no baseline edges to repository or business entities
