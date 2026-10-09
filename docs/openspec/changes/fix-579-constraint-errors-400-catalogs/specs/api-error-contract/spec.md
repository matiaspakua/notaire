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

### Requirement: Duplicates answer 409

A create or update whose data violates a unique constraint (SQLState 23505) SHALL answer 409 with the standard ErrorResponse and the message `The submitted data duplicates an existing record`, in both `ErrorResponses` and `GlobalExceptionHandler`; every other constraint violation SHALL keep 400 (Owner decision, Oct 9). Operations that can hit a unique constraint through client data SHALL document 409.

#### Scenario: Duplicate role name on update

- **WHEN** `PUT /api/v1/roles/{id}` renames a role to an existing role's name
- **THEN** the response is 409 with the duplicate message and no database text

#### Scenario: NOT NULL stays 400

- **WHEN** a catalog create sends {}
- **THEN** the response is still 400 with the constraint message

#### Scenario: Contract

- **WHEN** the OpenAPI document is generated
- **THEN** `PUT /roles/{id}`, `POST /minutas-inscripcion` and `POST /cuadernos` list a 409 response
