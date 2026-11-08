# procedure-folder-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1795-procedure-folder-hexagon. Update Purpose after archive.

## Requirements

### Requirement: ProcedureFolderController is ports-only

`ProcedureFolderController` MUST call inbound Query/Manage ports only and MUST NOT import entities or repositories. `ProcedureFolderOpsPort` MUST expose DTO shapes; the outbound adapter owns entity access.

#### Scenario: query via inbound port
- **WHEN** a client gets or searches procedure folders
- **THEN** the controller uses QueryProcedureFoldersUseCase and returns DTOs

#### Scenario: put on hold via inbound port
- **WHEN** a client puts a folder on hold
- **THEN** the controller uses ManageProcedureFoldersUseCase and maps sealed results to HTTP

#### Scenario: ArchUnit R1 shrinks
- **WHEN** hexagon baseline is checked
- **THEN** ProcedureFolderController entity edge is absent
