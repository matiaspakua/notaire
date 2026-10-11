# gestion-workflow-transicion Specification

## Purpose

Validates and applies a state change of a management against the transitions
defined in the `WorkflowDefinition` of its `ProcedureType` (CU83), instead
of accepting any status value unconditionally through the generic `PUT`.

## Requirements

### Requirement: Transition a management status against the defined workflow

The system SHALL validate that a proposed status change for a management
corresponds to an existing `WorkflowTransition` between the `WorkflowNode`
of the current status and the `WorkflowNode` of the destination status,
inside the `WorkflowDefinition` assigned to the management's `ProcedureType`,
per CU83. Status mutations after create MUST use
`POST /api/v1/managements/{id}/transition` (or archive); generic PUT and
complete-case update MUST NOT apply a changed status id.

#### Scenario: A valid transition is applied

- **WHEN** a user requests changing a management's status to a status for
  which a `WorkflowTransition` exists from its current status, in the
  `WorkflowDefinition` of its `ProcedureType`
- **THEN** the system applies the status change to the management

#### Scenario: An invalid transition is rejected

- **WHEN** a user requests changing a management's status to a status for
  which no `WorkflowTransition` exists from its current status, in the
  `WorkflowDefinition` of its `ProcedureType`
- **THEN** the system rejects the operation and responds with an error
  stating that the transition is not valid for that procedure type's workflow

#### Scenario: A management without a defined workflow rejects any transition

- **WHEN** a user requests changing the status of a management whose
  `ProcedureType` has no assigned `WorkflowDefinition`
- **THEN** the system rejects the operation and responds with an error
  stating that the procedure type has no workflow configured

### Requirement: Reject status mutations on generic update paths

The system SHALL reject any request that changes
`managementStatusId` / `statusManagementId` on an existing management via
`PUT /api/v1/managements/{id}` or `PUT /api/v1/managements/{id}/complete-case`,
responding with HTTP 400 and directing clients to
`POST /api/v1/managements/{id}/transition`. Updates that leave the status id
unchanged (or omit a status change) SHALL still succeed for other fields.
History coverage from #806 remains for create and for successful
`/transition` / archive paths.

#### Scenario: Plain PUT that changes status is rejected

- **WHEN** a client sends `PUT /api/v1/managements/{id}` with a
  `managementStatusId` different from the management's current status
- **THEN** the system responds 400 without persisting the status change

#### Scenario: Complete-case PUT that changes status is rejected

- **WHEN** a client sends `PUT /api/v1/managements/{id}/complete-case` with a
  `statusManagementId` different from the management's current status
- **THEN** the system responds 400 without persisting the status change

#### Scenario: Plain PUT that keeps the same status succeeds

- **WHEN** a client sends `PUT /api/v1/managements/{id}` with the same
  `managementStatusId` as the current status (or updates non-status fields)
- **THEN** the system applies the non-status updates successfully

### Requirement: Validate initial status against workflow nodes on create

When creating a management and a `WorkflowDefinition` is available (for
example a complete-case create whose `ProcedureType` has a workflow), the
system SHALL accept an initial status only if that status is a node in the
workflow, preferring the INITIAL/start node when clients choose among valid
nodes. Create without a workflow MAY assign any defined management status.
Initial assignment does not require a prior transition edge.

#### Scenario: Complete-case create with start-node status succeeds

- **WHEN** a client creates a complete-case management with
  `statusManagementId` equal to the workflow INITIAL node status
- **THEN** the system creates the management and records History for the
  initial status

#### Scenario: Complete-case create with status outside the workflow is rejected

- **WHEN** a client creates a complete-case management whose `ProcedureType`
  has a workflow, but `statusManagementId` is not any node in that workflow
- **THEN** the system responds 400 and does not create the management

### Requirement: Legal next destinations via workflow-trace

The system SHALL expose legal next destinations for a management by
`GET /api/v1/managements/{id}/workflow-trace`: clients derive valid
destination statuses from `transitions` whose `originNodeId` matches the
current node (the node whose status equals `currentStatus`). The managements
UI MUST offer only those destinations when changing status.

#### Scenario: Workflow-trace lists transitions usable as legal next states

- **WHEN** a client requests `GET /api/v1/managements/{id}/workflow-trace` for
  a management with a workflow and current status on a node that has outbound
  transitions
- **THEN** the response includes those transitions and nodes so the client
  can compute valid destinations without an unfiltered status catalog
