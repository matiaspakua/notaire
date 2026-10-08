# api-delete-status — delta

## Purpose

DELETE endpoints answer the success status documented in the OpenAPI contract.

## MODIFIED Requirements

### Requirement: DELETE answers the documented status

Every `@DeleteMapping` handler SHALL return exactly the 2xx status codes its OpenAPI annotations document; a successful delete of a resource SHALL answer `204 No Content` with no body.

#### Scenario: Documented and returned status agree

- **WHEN** the controller sources are scanned
- **THEN** every DELETE handler returns the success codes it documents and more than 25 handlers are checked

#### Scenario: Payment delete

- **WHEN** DELETE /api/v1/pagos/{id} removes an existing payment
- **THEN** the response is 204 with no body

#### Scenario: History delete

- **WHEN** an administrator deletes an existing history row
- **THEN** the response is 204 and the row is gone
