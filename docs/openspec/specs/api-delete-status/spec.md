# api-delete-status Specification

## Purpose
DELETE endpoints answer the success status documented in the OpenAPI contract.
## Requirements
### Requirement: Budget template delete answers 204

`DELETE /api/v1/budget-templates/procedure-types/{idProcedureType}/concept/{idConcept}` SHALL answer 204 No Content with no body when it removes the template, and 404 when it does not exist; every DELETE handler SHALL document 204 as its only success status.

#### Scenario: Delete answers 204

- **WHEN** an existing budget template is deleted
- **THEN** the response is 204 and the row is gone

#### Scenario: Every DELETE documents 204

- **WHEN** the controllers are scanned
- **THEN** each @DeleteMapping documents exactly one 2xx status, 204

### Requirement: DELETE answers the documented status

Every `@DeleteMapping` handler SHALL return exactly the 2xx status codes its OpenAPI annotations document; a successful delete of a resource SHALL answer `204 No Content` with no body.

#### Scenario: Documented and returned status agree

- **WHEN** the controller sources are scanned
- **THEN** every DELETE handler returns the success codes it documents and more than 25 handlers are checked

#### Scenario: Payment delete

- **WHEN** DELETE /api/v1/payments/{id} removes an existing payment
- **THEN** the response is 204 with no body

#### Scenario: History delete

- **WHEN** an administrator deletes an existing history row
- **THEN** the response is 204 and the row is gone

