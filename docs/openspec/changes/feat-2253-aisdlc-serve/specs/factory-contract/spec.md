## MODIFIED Requirements

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
