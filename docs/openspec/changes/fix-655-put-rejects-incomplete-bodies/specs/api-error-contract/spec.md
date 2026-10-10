# api-error-contract — delta

## Purpose

Consistent HTTP status codes and error bodies for failed API requests.

## MODIFIED Requirements

### Requirement: Empty or incomplete PUT bodies answer 400

`PUT /api/v1/testimonio/{id}`, `PUT /api/v1/tramites/{id}` and `PUT /api/v1/tipo-tramite/{id}/workflow` SHALL answer 400 with the standard ErrorResponse naming every missing required field (`<field>: es obligatorio`, joined by `; `) and SHALL store nothing. Required fields: `number`, `flagged`, `verified` (testimony); `idProcedureType` (procedure, as on POST); `workflowDefinitionId`, which MAY be null to unassign (workflow assignment). The contract SHALL mark them required and document 400.

#### Scenario: Empty testimony update

- **WHEN** `PUT /testimonio/{id}` sends {}
- **THEN** the response is 400 naming number, flagged and verified, and the stored testimony is unchanged

#### Scenario: Incomplete testimony update

- **WHEN** the body sends number and flagged only
- **THEN** the response is 400 naming verified only

#### Scenario: Procedure update without type

- **WHEN** `PUT /tramites/{id}` sends {} or only notes
- **THEN** the response is 400 `idProcedureType: es obligatorio` and the notes are unchanged

#### Scenario: Workflow assignment

- **WHEN** `PUT /tipo-tramite/{id}/workflow` sends {}
- **THEN** the response is 400 naming workflowDefinitionId; `{"workflowDefinitionId": null}` still unassigns with 200, and a non-integer id answers 400

#### Scenario: Contract

- **WHEN** the OpenAPI document is generated
- **THEN** the three PUTs list 400 and their request schemas list the required fields

### Requirement: A missing or stale version on update never answers 500

`PUT /api/v1/testimonio/{id}` SHALL require `version` (400 `version: es obligatorio` when absent) and SHALL answer 409 with the standard ErrorResponse ("The record was modified by another user; reload it and try again") when the sent version is not the stored one, storing nothing. Every update whose optimistic lock fails (Spring `OptimisticLockingFailureException`, JPA `OptimisticLockException`, Hibernate `StaleStateException`), whether caught through `ErrorResponses.updateFailed` or reaching `GlobalExceptionHandler`, SHALL answer that 409. The PUTs that copy `version` from the body (tipo-folio, estado-gestion, tipo-de-documento, tipo-tramite, conceptos, workflow-definition, movimiento-testimonio) SHALL document 409. Owner decision Oct 9.

#### Scenario: Testimony update without version

- **WHEN** `PUT /testimonio/{id}` sends number, flagged and verified but no version
- **THEN** the response is 400 `version: es obligatorio` and the testimony is unchanged

#### Scenario: Stale testimony version

- **WHEN** another update already moved the stored version past the one sent
- **THEN** the response is 409 and the newer data is kept

#### Scenario: Stale version on another versioned update

- **WHEN** a catalog or testimony-movement PUT sends a version that is not the stored one
- **THEN** the response is 409, not 500

#### Scenario: UI sends the version it read

- **WHEN** an administrator edits the same folio type twice
- **THEN** both saves answer 200 and notes and enabled are kept
