# factory-contract Specification

## Purpose

OpenAPI 3.1.0 factory control contract shared by CLI JSON and `aisdlc serve`
HTTP (#2253). One tree only.

## Requirements

### Requirement: OpenAPI 3.1.0 factory contract

The kit SHALL publish an OpenAPI 3.1.0 document covering config, plugins, harness,
report, catalogs, workflow/entity governance paths, and **`GET /factory/capabilities`**,
without importing the adopter application API. Schemas SHALL include FactoryEntity,
EntityVersion, Workflow, FactoryCapabilities (or equivalent), and extended catalog
kinds as needed. There SHALL be exactly one factory OpenAPI tree — Phase A.2 MUST
NOT introduce a second contract document.

**Phase B/C:** backoffice HTML/serve adapters continue to use the same catalog and
workflow paths as the system of record — no parallel schema set.

#### Scenario: contract check

- **WHEN** aisdlc contract check runs
- **THEN** it reports OpenAPI 3.1.0 OK

#### Scenario: capabilities path present

- **WHEN** the OpenAPI document is inspected after A.2
- **THEN** it contains `/factory/capabilities` and does not add a second OpenAPI file tree

#### Scenario: entity and workflow schemas present

- **WHEN** the OpenAPI document is inspected
- **THEN** it contains FactoryEntity, EntityVersion, Workflow, and extended kind enums

#### Scenario: contract check green with backoffice note

- **WHEN** `aisdlc contract check` runs after Phase B/C lands
- **THEN** it exits 0 and the contract still describes Workflow / catalog paths used by the backoffice

#### Scenario: no second OpenAPI write tree in Phase C

- **WHEN** `aisdlc contract check` runs after Phase C lands
- **THEN** the factory OpenAPI remains a single tree and Phase C does not add
  competing `/factory/*` write schemas for A.2/A.3 ownership

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

### Requirement: One OpenAPI tree documents A.3 writes

`openapi/factory-control.openapi.yaml` SHALL remain the single factory contract
tree. It SHALL document `If-Match` on catalog PUT, `schema_version` /
`created_by` on the entity envelope, workflow body v2 schemas, and 409
version-conflict Problem responses. No second OpenAPI write tree SHALL be added.

#### Scenario: If-Match and schema fields appear in OpenAPI

- **WHEN** `aisdlc contract check` runs after this change
- **THEN** the factory OpenAPI declares If-Match on catalog PUT and envelope
  fields `schema_version` and `created_by` without introducing a second tree

### Requirement: One OpenAPI tree documents Project Run and RunRequest

`openapi/factory-control.openapi.yaml` SHALL remain the single factory contract
tree. It SHALL document Project, Run, and RunRequest schemas plus
`/factory/projects` and `/factory/runs` paths (including events and cancel).
No second OpenAPI tree SHALL be added.

#### Scenario: OpenAPI Project Run RunRequest schemas present

- **WHEN** `aisdlc contract check` runs after this change
- **THEN** the factory OpenAPI declares Project, Run, and RunRequest schemas and
  the project/run paths without introducing a second tree
