# api-error-contract — delta

## Purpose

Consistent HTTP status codes and error bodies for failed API requests.

## MODIFIED Requirements

### Requirement: Workflow creates and updates answer 400 for missing or invalid data

The workflow definition, node and transition create and update endpoints SHALL answer 400 with the standard ErrorResponse when a required field is missing, a node type is unknown, a node update carries nothing to change, or the data violates a database constraint; SHALL keep 409 or 500 for other failures; SHALL never echo the database text; and SHALL document 400 on POST and PUT.

#### Scenario: Create with an empty body

- **WHEN** a workflow definition, node or transition create sends {}
- **THEN** the response is 400 naming the problem and nothing is stored

#### Scenario: Update breaking a constraint or missing a node id

- **WHEN** a definition update has no name, or a transition update has no origin or destination node
- **THEN** the response is 400

#### Scenario: Node update with nothing to change or an unknown type

- **WHEN** a node update sends {} or an unknown type
- **THEN** the response is 400, and a position-only update still answers 200

#### Scenario: Contract

- **WHEN** the OpenAPI document is generated
- **THEN** POST and PUT of each workflow resource list a 400 response
