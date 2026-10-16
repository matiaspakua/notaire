# r1-wire-dtos Specification

## Purpose
TBD - created by archiving change 2026-10-16-refactor-1826-r1-wire-dtos. Update Purpose after archive.

## Requirements

### Requirement: Inbound wire DTOs do not depend on JPA entities

`ItemResponse` and support `*Ref` / `ErrorResponses` in `adapter.in.web` SHALL NOT import or reference `business.*` entities or `jakarta.persistence` types. Mapping SHALL use views, IDs, or framework-agnostic failure detection.

#### Scenario: ItemResponse has no Item or Budget entity import
- **WHEN** ArchUnit R1 runs after this slice
- **THEN** ItemResponse→Item and ItemResponse→Budget baseline edges are absent

#### Scenario: Support refs have no entity imports
- **WHEN** ArchUnit R1 runs after this slice
- **THEN** DeedRef, DocumentTypeRef, ManagementRef, NotaryRef, ProcedureTypeRef, and PropertyRef have no business.* edges

#### Scenario: ErrorResponses has no jakarta.persistence edge
- **WHEN** ArchUnit R1 runs after this slice
- **THEN** ErrorResponses→OptimisticLockException baseline edge is absent

#### Scenario: REST contract unchanged
- **WHEN** clients call item CRUD and budget items-from-template / items-from-catalog
- **THEN** status codes and JSON field names match the pre-slice behaviour
