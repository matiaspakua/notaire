# person-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1813-person-hexagon. Update Purpose after archive.

## Requirements

### Requirement: PersonController is ports-only

The person mixed controller SHALL call inbound Query/Manage ports only. Entity access, identification-type resolution, and duplicate conflict handling SHALL live behind PersonOpsPort.

#### Scenario: Controller has no entity or repository imports
- **WHEN** ArchUnit R1 runs after this slice
- **THEN** PersonController baseline edges to Person, IdentificationType, and IdentificationTypeRepository are absent

#### Scenario: REST contract unchanged
- **WHEN** clients call /api/v1/people CRUD and search
- **THEN** status codes and JSON shapes match the pre-slice behaviour (including duplicate conflict body)
