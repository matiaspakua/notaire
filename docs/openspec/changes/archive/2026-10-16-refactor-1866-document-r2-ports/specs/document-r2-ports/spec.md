## ADDED Requirements

### Requirement: Document R2 ports are entity-free

The document application slice MUST NOT depend on `business.*` entities or Spring Data repositories. Outbound ports speak domain views only (or are deleted when helpers move to adapters). Controllers call inbound ports only (`ExternalEntityDocumentUseCase`, `QueryUpcomingExpirationsUseCase`). Entity-bound helpers live under `adapter.out.persistence`. Entity-speaking `DocumentRepositoryPort` and `ExternalDocumentRepositoryPort` SHALL be removed.

#### Scenario: R2 baseline shrinks for document
- **WHEN** HexagonArchitectureTest runs against the updated tree
- **THEN** the 19 document R2 baseline edges for DocumentRepositoryPort, ExternalDocumentRepositoryPort, DocumentCostTemplateService, ExternalEntityDocumentService, and UpcomingExpirationService are gone

#### Scenario: Controllers stay R5-clean
- **WHEN** ManagementController loads external-entity documents or UpcomingExpirationController lists expirations
- **THEN** those adapters inject inbound ports only (no outbound document helpers)
