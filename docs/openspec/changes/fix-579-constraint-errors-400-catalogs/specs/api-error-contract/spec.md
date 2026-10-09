# api-error-contract — delta

## Purpose

Consistent HTTP status codes and error bodies for failed API requests.

## MODIFIED Requirements

### Requirement: Catalog constraint errors answer 400

The catalog create and update endpoints SHALL answer 400 with the standard ErrorResponse and the message `The submitted data violates a database constraint` when the request data violates a database constraint, SHALL keep 409 (create) and 500 (update) for other failures, SHALL never echo the database text, and SHALL document 400 on POST and PUT.

#### Scenario: Create violating a constraint

- **WHEN** a catalog create sends {}
- **THEN** the response is 400 with the constraint message and nothing is stored

#### Scenario: Update violating a constraint

- **WHEN** a full update sets the name to null
- **THEN** the response is 400 with the constraint message

#### Scenario: Other failures

- **WHEN** a create or update fails for another reason
- **THEN** the response keeps 409 or 500 with a generic message

#### Scenario: Contract

- **WHEN** the OpenAPI document is generated
- **THEN** POST and PUT of each catalog list a 400 response
