# workflow-definition-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1728-workflow-definition-hexagon. Update Purpose after archive.

## Requirements

### Requirement: Workflow definitions are managed through domain-typed ports

The workflow-definition HTTP adapter MUST call inbound ports only. Use cases MUST own the delete-guard when associated nodes exist.

#### Scenario: CRUD keeps the wire contract

- **WHEN** a client lists, gets, creates, updates, or deletes a workflow definition
- **THEN** status codes and JSON fields match the pre-refactor contract (update remains empty 200 body)

#### Scenario: delete with nodes is refused

- **WHEN** a workflow definition has associated nodes and the client deletes
- **THEN** the response is 409 with the existing error body

#### Scenario: missing id answers 404

- **WHEN** get, update, or delete targets an absent id
- **THEN** the response is 404

#### Scenario: controller has no persistence imports

- **WHEN** ArchUnit R1 runs after the slice
- **THEN** WorkflowDefinitionController has no baseline edges to repository or business entities
