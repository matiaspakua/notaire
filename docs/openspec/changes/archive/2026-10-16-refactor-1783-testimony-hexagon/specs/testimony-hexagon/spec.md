## ADDED Requirements

### Requirement: Testimony HTTP adapter is ports-only

`TestimonyController` MUST call inbound ports only. It MUST NOT import `repository.*`, `jpa.*`, or `business.*`. `@Transactional` MUST NOT remain on the controller. REST paths under `/api/v1/testimonies` MUST keep the same status codes and JSON shapes (including #655 required fields, optimistic lock 409, #1364 in-use delete 409, generate/verify). ArchUnit R1 MUST clear the TestimonyController baseline edges.

#### Scenario: list and get testimonies

- **WHEN** a client lists testimonies or gets one by id
- **THEN** the API answers with DtoTestimony shapes (200 / 404)

#### Scenario: create testimony with deed

- **WHEN** a client POSTs a valid create body with an existing deed id
- **THEN** the API creates the row and answers 201

#### Scenario: delete blocked by movements or copies

- **WHEN** a client deletes a testimony that has registry movements or issued copies
- **THEN** the API answers 409 with the existing in-use message

#### Scenario: controller has no repository or entity imports

- **WHEN** ArchUnit R1 runs after the slice
- **THEN** TestimonyController has no baseline edges to Spring Data repositories or business entities
