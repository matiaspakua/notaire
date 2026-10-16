# procedure-template-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1798-procedure-template-hexagon. Update Purpose after archive.

## Requirements

### Requirement: ProcedureTemplateController is ports-only

`ProcedureTemplateController` MUST call inbound Query/Manage ports only and MUST NOT import entities or repositories. `ProcedureTemplateOpsPort` MUST expose DTO/view shapes; the outbound adapter owns entity access.

#### Scenario: query via inbound port
- **WHEN** a client lists or gets procedure templates
- **THEN** the controller uses QueryProcedureTemplatesUseCase

#### Scenario: writes via inbound port
- **WHEN** a client creates, updates, or deletes a template
- **THEN** the controller uses ManageProcedureTemplatesUseCase and maps sealed results to HTTP

#### Scenario: ArchUnit R1 shrinks
- **WHEN** hexagon baseline is checked
- **THEN** ProcedureTemplateController entity edges are absent
