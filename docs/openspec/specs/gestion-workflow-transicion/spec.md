# gestion-workflow-transicion Specification

## Purpose

Validates and applies a state change of a gestión against the transitions
defined in the `WorkflowDefinition` of its tipo de trámite (CU83), instead
of accepting any estado value unconditionally through the generic `PUT`.

## Requirements

### Requirement: Transicionar estado de gestión contra el workflow definido

El sistema SHALL validar que un cambio de estado propuesto para una
gestión corresponda a una `WorkflowTransition` existente entre el
`WorkflowNode` del estado actual y el `WorkflowNode` del estado destino,
dentro del `WorkflowDefinition` asignado al `TipoDeTramite` de la gestión,
según CU83. Status mutations after create MUST use
`POST /api/v1/gestiones/{id}/transition` (or archive); generic PUT and
complete-case update MUST NOT apply a changed status id.

#### Scenario: Transición válida se aplica

- **WHEN** un usuario solicita cambiar el estado de una gestión a un
  estado para el cual existe una `WorkflowTransition` desde su estado
  actual, en el `WorkflowDefinition` de su tipo de trámite
- **THEN** el sistema aplica el cambio de estado a la gestión

#### Scenario: Transición inválida es rechazada

- **WHEN** un usuario solicita cambiar el estado de una gestión a un
  estado para el cual no existe ninguna `WorkflowTransition` desde su
  estado actual, en el `WorkflowDefinition` de su tipo de trámite
- **THEN** el sistema rechaza la operación y responde con un error que
  indica que la transición no es válida para el workflow del tipo de
  trámite

#### Scenario: Gestión sin workflow definido rechaza cualquier transición

- **WHEN** un usuario solicita cambiar el estado de una gestión cuyo tipo
  de trámite no tiene un `WorkflowDefinition` asignado
- **THEN** el sistema rechaza la operación y responde con un error que
  indica que el tipo de trámite no tiene un workflow configurado

### Requirement: Reject status mutations on generic update paths

The system SHALL reject any request that changes
`managementStatusId` / `statusManagementId` on an existing gestión via
`PUT /api/v1/gestiones/{id}` or `PUT /api/v1/gestiones/{id}/complete-case`,
responding with HTTP 400 and directing clients to
`POST /api/v1/gestiones/{id}/transition`. Updates that leave the status id
unchanged (or omit a status change) SHALL still succeed for other fields.
History (bitácora) coverage from #806 remains for create and for successful
`/transition` / archive paths.

#### Scenario: Plain PUT that changes status is rejected

- **WHEN** a client sends `PUT /api/v1/gestiones/{id}` with a
  `managementStatusId` different from the gestión's current status
- **THEN** the system responds 400 without persisting the status change

#### Scenario: Complete-case PUT that changes status is rejected

- **WHEN** a client sends `PUT /api/v1/gestiones/{id}/complete-case` with a
  `statusManagementId` different from the gestión's current status
- **THEN** the system responds 400 without persisting the status change

#### Scenario: Plain PUT that keeps the same status succeeds

- **WHEN** a client sends `PUT /api/v1/gestiones/{id}` with the same
  `managementStatusId` as the current status (or updates non-status fields)
- **THEN** the system applies the non-status updates successfully

### Requirement: Validate initial status against workflow nodes on create

When creating a gestión and a `WorkflowDefinition` is available (e.g.
complete-case with a tipo de trámite that has a workflow), the system SHALL
accept an initial status only if that status is a node in the workflow,
preferring the INITIAL/start node when clients choose among valid nodes.
Create without a workflow MAY assign any defined management status.
Initial assignment does not require a prior transition edge.

#### Scenario: Complete-case create with start-node status succeeds

- **WHEN** a client creates a complete-case gestión with
  `statusManagementId` equal to the workflow INITIAL node status
- **THEN** the system creates the gestión and records History for the
  initial status

#### Scenario: Complete-case create with status outside the workflow is rejected

- **WHEN** a client creates a complete-case gestión whose tipo de trámite
  has a workflow, but `statusManagementId` is not any node in that workflow
- **THEN** the system responds 400 and does not create the gestión

### Requirement: Legal next destinations via workflow-trace

The system SHALL expose legal next destinations for a gestión by
`GET /api/v1/gestiones/{id}/workflow-trace`: clients derive valid
destination statuses from `transitions` whose `originNodeId` matches the
current node (the node whose status equals `statusActual`). The gestiones
UI MUST offer only those destinations when changing status.

#### Scenario: Workflow-trace lists transitions usable as legal next states

- **WHEN** a client requests `GET /api/v1/gestiones/{id}/workflow-trace` for
  a gestión with a workflow and current status on a node that has outbound
  transitions
- **THEN** the response includes those transitions and nodes so the client
  can compute valid destinations without an unfiltered status catalog
