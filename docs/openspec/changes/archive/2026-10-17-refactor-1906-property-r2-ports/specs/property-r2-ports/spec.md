## ADDED Requirements

### Requirement: Property application ports stay entity-free

The property application slice MUST NOT depend on JPA entities via entity-speaking ports. Entity access for procedure ops lives in outbound persistence adapters; HTTP uses PropertyCatalogPort.

#### Scenario: Entity RepositoryPort removed
- **GIVEN** PropertyRepositoryPort exposed Property entities
- **WHEN** the R2 shrink lands
- **THEN** that port and PropertyPersistenceAdapter are deleted

#### Scenario: Catalog port remains domain-safe
- **WHEN** PropertyController queries or mutates properties
- **THEN** it uses inbound ports backed by PropertyCatalogPort (domain types only)

#### Scenario: ArchUnit R2 baseline reaches zero
- **GIVEN** one R2 baseline edge for PropertyRepositoryPort
- **WHEN** HexagonArchitectureTest runs after the change
- **THEN** no R2 edges remain
