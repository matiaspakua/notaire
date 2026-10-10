# api-delete-status — delta

## Purpose

DELETE endpoints answer the success status documented in the OpenAPI contract.

## MODIFIED Requirements

### Requirement: Budget template delete answers 204

`DELETE /api/v1/plantilla-presupuestos/tipo-tramite/{idProcedureType}/concepto/{idConcept}` SHALL answer 204 No Content with no body when it removes the template, and 404 when it does not exist; every DELETE handler SHALL document 204 as its only success status.

#### Scenario: Delete answers 204

- **WHEN** an existing budget template is deleted
- **THEN** the response is 204 and the row is gone

#### Scenario: Every DELETE documents 204

- **WHEN** the controllers are scanned
- **THEN** each @DeleteMapping documents exactly one 2xx status, 204
