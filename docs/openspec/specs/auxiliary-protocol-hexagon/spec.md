# auxiliary-protocol-hexagon Specification

## Purpose
TBD - created by archiving change 2026-10-16-refactor-1825-auxiliary-protocol-hexagon. Update Purpose after archive.

## Requirements

### Requirement: AuxiliaryProtocolController is ports-only

The auxiliary-protocol mixed controller SHALL call inbound Query/Manage ports for list-available-folios and start-deed. Entity access SHALL live behind AuxiliaryProtocolOpsPort. The HTTP response for start-deed SHALL be a wire DTO mapped from a port view (not a JPA entity).

#### Scenario: Controller has no Deed entity import
- **WHEN** ArchUnit R1 runs after this slice
- **THEN** AuxiliaryProtocolController has no baseline edge to `business.Deed`

#### Scenario: REST contract unchanged
- **WHEN** clients call `/api/v1/auxiliary-protocol/available-folios` and `/api/v1/auxiliary-protocol/deeds`
- **THEN** status codes and JSON shapes match the pre-slice behaviour (including `idDeed` on create)
