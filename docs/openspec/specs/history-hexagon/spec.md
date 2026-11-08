# history-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1775-history-hexagon. Update Purpose after archive.

## Requirements

### Requirement: History HTTP adapter is ports-only

`HistoryController` MUST call inbound ports only. It MUST NOT import `repository.*`, `jpa.*`, or `business.*`. `@Transactional` MUST NOT remain on the controller. REST paths under `/api/v1/history` MUST keep the same status codes and JSON shapes (including CU13 chronological order for management history). ArchUnit R1 MUST clear the HistoryController baseline edges.

#### Scenario: list and get history summaries

- **WHEN** a client lists history (paged) or gets a history row by id
- **THEN** the API answers with the existing summary DTO shapes (200 / 404)

#### Scenario: get history by management

- **WHEN** a client GETs `/api/v1/history/management/{idManagement}`
- **THEN** the API answers 200 with history rows in CU13 chronological order

#### Scenario: create history with valid dependencies

- **WHEN** a client POSTs a valid history body with known status and management ids
- **THEN** the API creates the row and answers 201 with the summary body

#### Scenario: create history with missing dependencies

- **WHEN** a client POSTs a history body with unknown status or management id
- **THEN** the API answers 400

#### Scenario: controller has no repository or entity imports

- **WHEN** ArchUnit R1 runs after the slice
- **THEN** HistoryController has no baseline edges to Spring Data repositories or business entities
