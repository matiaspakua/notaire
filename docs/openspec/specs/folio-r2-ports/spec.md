# folio-r2-ports Specification

## Purpose
ArchUnit R2 shrink for the folio application slice: no entity-speaking ports; Spring Data stays in outbound persistence adapters (#1903 / ADR-030).

## Requirements

### Requirement: Folio application ports stay entity-free

The folio application slice MUST NOT depend on JPA entities or Spring Data repositories via entity-speaking ports. Entity orchestration for auxiliary-protocol helpers lives in outbound persistence adapters behind view-safe catalog ports for the HTTP path.

#### Scenario: Entity helper lives in outbound adapter
- **GIVEN** AuxiliaryProtocolService needs Folio entities for startDeed
- **WHEN** the R2 shrink lands
- **THEN** the helper injects Spring Data FolioRepository locally under adapter.out.persistence.notebook

#### Scenario: Entity RepositoryPort removed
- **GIVEN** FolioRepositoryPort exposed Folio and FolioType entities
- **WHEN** the R2 shrink lands
- **THEN** that port and FolioPersistenceAdapter are deleted

#### Scenario: Catalog ports remain domain-safe
- **WHEN** FolioController queries or mutates folios
- **THEN** it uses inbound ports backed by FolioCatalogPort / FolioTypeCatalogPort (domain types only)

#### Scenario: ArchUnit R2 baseline shrinks
- **GIVEN** two R2 baseline edges for FolioRepositoryPort
- **WHEN** HexagonArchitectureTest runs after the change
- **THEN** those edges are gone and no new R2 violations appear
