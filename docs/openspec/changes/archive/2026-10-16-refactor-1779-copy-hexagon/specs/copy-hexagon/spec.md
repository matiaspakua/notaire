## ADDED Requirements

### Requirement: Copy HTTP adapter is ports-only

`CopyController` MUST call inbound ports only. It MUST NOT import `repository.*`, `jpa.*`, or `business.*`. `@Transactional` MUST NOT remain on the controller. REST paths under `/api/v1/copies` MUST keep the same status codes and JSON shapes (including CU87 conflict when a testimony already has a registered movement). ArchUnit R1 MUST clear the CopyController baseline edges.

#### Scenario: list and get copies

- **WHEN** a client lists copies or gets a copy by id
- **THEN** the API answers with the existing response shapes (200 / 404)

#### Scenario: create copy with valid body

- **WHEN** a client POSTs a valid copy body
- **THEN** the API creates the row and answers 201

#### Scenario: create copy blocked by registered testimony movement

- **WHEN** a client POSTs a copy for a testimony that already has a registered movement
- **THEN** the API answers 409 with the existing error body

#### Scenario: controller has no repository or entity imports

- **WHEN** ArchUnit R1 runs after the slice
- **THEN** CopyController has no baseline edges to Spring Data repositories or business entities
