# api-error-contract — delta

## Purpose

Consistent HTTP status codes and error bodies for failed API requests.

## MODIFIED Requirements

### Requirement: Testimony and person constraint errors answer 400

The testimony, testimony movement and person create and update endpoints SHALL answer 400 with the standard ErrorResponse and the message `The submitted data violates a database constraint` when the request data violates a database constraint, SHALL keep their previous status for other failures, SHALL never echo the database text, and SHALL document 400 on POST and PUT.

#### Scenario: Empty testimony movement

- **WHEN** a testimony movement create sends {}
- **THEN** the response is 400 with the constraint message and nothing is stored

#### Scenario: Constraint violation on create or update

- **WHEN** saving a testimony, testimony movement or person violates a database constraint
- **THEN** the response is 400 with the constraint message and no SQL text

#### Scenario: Other failures

- **WHEN** a create or update fails for another reason
- **THEN** the response keeps 409 (creates, person update) or 500 (testimony and movement updates)

#### Scenario: Contract

- **WHEN** the OpenAPI document is generated
- **THEN** POST and PUT of testimonies, testimony movements and people list a 400 response
