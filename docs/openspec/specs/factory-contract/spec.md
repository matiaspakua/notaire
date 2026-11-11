# factory-contract Specification

## Purpose
OpenAPI 3.1.0 factory control contract shared by CLI JSON and future HTTP. Update Purpose after archive.

## Requirements

### Requirement: OpenAPI 3.1.0 factory contract
The kit SHALL publish an OpenAPI 3.1.0 document covering config, plugins, harness,
report, catalogs, and workflow/entity governance paths without importing the
adopter application API. Schemas SHALL include FactoryEntity, EntityVersion,
Workflow, and extended catalog kinds as needed for Phase A.
**Phase B:** the contract description SHALL note that the backoffice HTML/serve
adapter uses those same catalog/workflow paths as the system of record — no
parallel schema set.

#### Scenario: contract check
- **WHEN** aisdlc contract check runs
- **THEN** it reports OpenAPI 3.1.0 OK

#### Scenario: entity and workflow schemas present
- **WHEN** the OpenAPI document is inspected
- **THEN** it contains FactoryEntity, EntityVersion, Workflow, and extended kind enums

#### Scenario: contract check green with backoffice note
- **WHEN** `aisdlc contract check` runs after Phase B lands
- **THEN** it exits 0 and the contract still describes Workflow / catalog paths used by the backoffice

### Requirement: CLI JSON aligns with FactoryConfig
Required FactoryConfig properties SHALL be present on config show --json payloads.

#### Scenario: required keys
- **WHEN** config_view is compared to the contract
- **THEN** no required key is missing

### Requirement: Breaking-change accept path
An accepted-breaking-changes directory SHALL exist for oasdiff suppressions.

#### Scenario: accept dir
- **WHEN** the kit is verified
- **THEN** openapi/accepted-breaking-changes.d exists with a README

### Requirement: Phase C backoffice adapts catalogs and evidence without a second SoR
The kit OpenAPI / CLI contract SHALL remain the system of record for catalogs and
evidence. Phase C backoffice HTML/serve adapters MUST NOT introduce a second
OpenAPI tree, a parallel metrics store, or competing write semantics for paths
owned by Phase A.2 (`aisdlc serve`) / A.3 (`If-Match` writes). Descriptive notes
MAY document that Tools/MCP, Providers, Knowledge, Rules/Gates, Integrations,
and Runs UI surfaces adapt existing catalog and evidence/report operations.

#### Scenario: no second OpenAPI write tree in Phase C
- **WHEN** `aisdlc contract check` runs after Phase C lands
- **THEN** the factory OpenAPI remains a single tree and Phase C does not add
  competing `/factory/*` write schemas for A.2/A.3 ownership
