# management-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1772-management-hexagon. Update Purpose after archive.

## Requirements

### Requirement: Management HTTP adapter is ports-only

`ManagementController` and `TransitionManagementWebMapper` MUST call inbound ports / application facades only. They MUST NOT import `repository.*`, `jpa.*`, or `business.*`. `@Transactional` MUST NOT remain on the controller. REST paths under `/api/v1/managements` MUST keep the same status codes and JSON shapes. ArchUnit R1 MUST clear the ManagementController and TransitionManagementWebMapper baseline edges.

#### Scenario: list and get management summaries

- **WHEN** a client lists or gets a management by id or number
- **THEN** the API answers with the existing summary DTO shapes (200 / 404)

#### Scenario: create complete case

- **WHEN** a client POSTs a valid complete-case body
- **THEN** the API creates the management and answers 201 with the summary body

#### Scenario: invalid complete-case dependencies

- **WHEN** a client POSTs a complete-case body with missing required fields or unknown dependency ids
- **THEN** the API answers 400

#### Scenario: transition mapper has no repository imports

- **WHEN** ArchUnit R1 runs after the slice
- **THEN** TransitionManagementWebMapper has no baseline edges to DeedManagementRepository or business entities

#### Scenario: controller has no repository or entity imports

- **WHEN** ArchUnit R1 runs after the slice
- **THEN** ManagementController has no baseline edges to Spring Data repositories or business entities
