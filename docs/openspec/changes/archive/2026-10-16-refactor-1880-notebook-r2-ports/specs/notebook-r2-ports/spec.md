## ADDED Requirements

### Requirement: Notebook R2 ports are entity-free
The notebook application slice MUST NOT depend on `business.*` entities or Spring Data repositories via entity-speaking ports. Ops adapters own entity-bound helpers locally. Controllers call inbound/ops ports only.

#### Scenario: R2 baseline shrinks for notebook
- **WHEN** HexagonArchitectureTest runs against the updated tree
- **THEN** the ~10 notebook R2 baseline edges for NotebookRepositoryPort, DeedOperationPort, NotebookFolioOperationPort, NotebookService, and AuxiliaryProtocolService are gone

#### Scenario: Live OpsPorts remain view-safe
- **WHEN** NotebookController / AuxiliaryProtocolController create notebooks or start auxiliary deeds
- **THEN** they use NotebookOpsPort / AuxiliaryProtocolOpsPort (views only); helpers live under adapter.out.persistence.notebook
