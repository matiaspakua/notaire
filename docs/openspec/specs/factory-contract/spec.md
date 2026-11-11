# factory-contract Specification

## Purpose
OpenAPI 3.1.0 factory control contract shared by CLI JSON and future HTTP. Update Purpose after archive.

## Requirements

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
