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
