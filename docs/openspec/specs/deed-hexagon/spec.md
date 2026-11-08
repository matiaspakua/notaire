# deed-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1811-deed-hexagon. Update Purpose after archive.

## Requirements

### Requirement: Deed controller is ports-only
The deed HTTP adapter SHALL call inbound Query/Manage ports only; entity and FolioRepository access SHALL live behind DeedOpsPort.

#### Scenario: query via inbound port
- **WHEN** a client lists, fetches, searches, or lists available notaries
- **THEN** the controller delegates to QueryDeedsUseCase and returns mapped views

#### Scenario: writes and sign via inbound port
- **WHEN** a client creates, updates, deletes, or signs a deed
- **THEN** the controller delegates to ManageDeedsUseCase; status vocabulary and folio link stay in the adapter

#### Scenario: ArchUnit R1 shrinks
- **WHEN** HexagonArchitectureTest runs
- **THEN** DeedController has no R1 baseline edges to Deed, Folio, or FolioRepository
