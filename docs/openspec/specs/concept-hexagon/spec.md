# concept-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1815-concept-hexagon. Update Purpose after archive.

## Requirements

### Requirement: ConceptController is ports-only

The concept mixed controller SHALL call inbound Query/Manage ports only. Entity access and BudgetTemplateRepository in-use checks SHALL live behind ConceptOpsPort.

#### Scenario: Controller has no entity or repository imports
- **WHEN** ArchUnit R1 runs after this slice
- **THEN** ConceptController baseline edges to Concept and BudgetTemplateRepository are absent

#### Scenario: REST contract unchanged
- **WHEN** clients call /api/v1/concepts CRUD, search, and in-use
- **THEN** status codes and JSON shapes match the pre-slice behaviour
