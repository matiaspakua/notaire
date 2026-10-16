# document-cost-template-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1821-document-cost-template-hexagon. Update Purpose after archive.

## Requirements

### Requirement: DocumentCostTemplateController is ports-only

`DocumentCostTemplateController` MUST NOT import JPA entities. Create and list-by-procedure-type MUST go through inbound ports backed by an outbound OpsPort.

#### Scenario: list via query port
- **WHEN** a client GETs `/api/v1/document-cost-templates/procedure-types/{id}`
- **THEN** the controller calls `QueryDocumentCostTemplatesUseCase` only

#### Scenario: create via manage port
- **WHEN** a client POSTs `/api/v1/document-cost-templates`
- **THEN** the controller calls `ManageDocumentCostTemplatesUseCase` only and maps sealed results to HTTP

#### Scenario: ArchUnit R1 shrinks
- **WHEN** ArchUnit runs against the hexagon baseline
- **THEN** DocumentCostTemplateController → DocumentCostTemplate / DocumentCostTemplatePK R1 edges are absent
