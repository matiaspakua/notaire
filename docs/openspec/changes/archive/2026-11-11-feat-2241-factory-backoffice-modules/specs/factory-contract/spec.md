# factory-contract Delta — Phase C (#2241)

## ADDED Requirements

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
