## ADDED Requirements

### Requirement: Procedure R2 ports are entity-free
The procedure application slice MUST NOT depend on `business.*` entities or Spring Data repositories via entity-speaking ports. Ops adapters use Spring Data locally. Controllers call inbound/ops ports only.

#### Scenario: R2 baseline shrinks for procedure
- **WHEN** HexagonArchitectureTest runs against the updated tree
- **THEN** the ~17 procedure R2 baseline edges for Procedure*RepositoryPort, ProcedureFolderService, and RequiredDocumentationOnConfirmService are gone

#### Scenario: ProcedureLookupPort remains view-safe
- **WHEN** SubmittedDocumentManagementService checks procedure existence
- **THEN** it uses ProcedureLookupPort (boolean only)