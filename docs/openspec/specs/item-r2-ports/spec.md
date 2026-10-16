# item-r2-ports Specification

## Purpose
ArchUnit R2 shrink for the item application slice: no entity-speaking ports; Spring Data stays in outbound persistence adapters (#1904 / ADR-030).

## Requirements

### Requirement: Item application ports stay entity-free

The item application slice MUST NOT depend on JPA entities or Spring Data repositories via entity-speaking ports. Entity orchestration lives in outbound persistence adapters behind view-safe ops ports.

#### Scenario: Entity helper lives in outbound adapter
- **GIVEN** ItemService needs Item entities
- **WHEN** the R2 shrink lands
- **THEN** the helper is under adapter.out.persistence.item and injects Spring Data locally

#### Scenario: Entity RepositoryPort removed
- **GIVEN** ItemRepositoryPort exposed Item entities
- **WHEN** the R2 shrink lands
- **THEN** that port and its PersistenceAdapter are deleted

#### Scenario: OpsPort remains view-safe
- **WHEN** ItemController queries or mutates items
- **THEN** it uses inbound ports backed by ItemOpsPort (DTO views only)

#### Scenario: ArchUnit R2 baseline shrinks
- **GIVEN** two R2 baseline edges for item
- **WHEN** HexagonArchitectureTest runs after the change
- **THEN** those edges are gone and no new R2 violations appear
