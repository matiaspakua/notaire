## ADDED Requirements

### Requirement: Workflow nodes are managed through domain-typed ports

The workflow-node HTTP adapter MUST call inbound ports only. Use cases MUST own the delete-guard when associated transitions exist.

#### Scenario: CRUD keeps the wire contract

- **WHEN** a client lists by workflow, gets, creates, updates, or deletes a workflow node
- **THEN** status codes and JSON fields match the pre-refactor contract (update remains empty 200 body)

#### Scenario: delete with transitions is refused

- **WHEN** a node has associated transitions and the client deletes
- **THEN** the response is 409 with the existing error body

#### Scenario: missing workflow or status on create answers 404

- **WHEN** create references an absent workflow definition or management status
- **THEN** the response is 404

#### Scenario: controller has no persistence imports

- **WHEN** ArchUnit R1 runs after the slice
- **THEN** WorkflowNodeController has no baseline edges to repository or business entities
