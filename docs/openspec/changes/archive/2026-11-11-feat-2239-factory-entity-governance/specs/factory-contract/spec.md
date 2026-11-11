## MODIFIED Requirements

### Requirement: OpenAPI 3.1.0 factory contract
The kit SHALL publish an OpenAPI 3.1.0 document covering config, plugins, harness,
report, catalogs, and workflow/entity governance paths without importing the
adopter application API. Schemas SHALL include FactoryEntity, EntityVersion,
Workflow, and extended catalog kinds as needed for Phase A.

#### Scenario: contract check
- **WHEN** aisdlc contract check runs
- **THEN** it reports OpenAPI 3.1.0 OK

#### Scenario: entity and workflow schemas present
- **WHEN** the OpenAPI document is inspected
- **THEN** it contains FactoryEntity, EntityVersion, Workflow, and extended kind enums
