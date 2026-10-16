# deed-r2-ports Specification

## Purpose
TBD - created by archiving change 2026-10-16-refactor-1852-deed-r2-ports. Update Purpose after archive.

## Requirements

### Requirement: Deed application ports speak ids/views only

Deed inbound numbering ports SHALL accept `Integer notaryId` (not `business.Person`). Entity-bound deed persistence helpers SHALL live under `adapter.out.persistence.deed`, not `application.usecase.deed`.

#### Scenario: Numbering ports have no Person import
- **WHEN** ArchUnit R2 runs after this slice
- **THEN** `CalculateNextDeedNumberUseCase` and `ValidateDeedNumberingUseCase` have no baseline edges to `business.Person`

#### Scenario: DeedService left the application layer
- **WHEN** ArchUnit R2 runs after this slice
- **THEN** no R2 edges originate from `application.usecase.deed.DeedService` or `DeedSigningService`

#### Scenario: Numbering behaviour unchanged
- **WHEN** clients validate or calculate the next deed number for a notary/year/protocol
- **THEN** OK / DUPLICATE / SKIP_* results and next-sequence values match the pre-slice behaviour
