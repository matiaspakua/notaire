# user-r2-ports Specification

## Purpose
ArchUnit R2 shrink for the user application slice: no entity-speaking ports; catalog ports stay domain-safe (#1905 / ADR-030).

## Requirements

### Requirement: User application ports stay entity-free

The user application slice MUST NOT depend on JPA entities via entity-speaking ports. Domain catalog ports remain the outbound contracts for user and role storage.

#### Scenario: Entity RepositoryPorts removed
- **GIVEN** UserRepositoryPort and RoleRepositoryPort exposed User and Role entities
- **WHEN** the R2 shrink lands
- **THEN** those ports and their PersistenceAdapters are deleted

#### Scenario: Catalog ports remain domain-safe
- **WHEN** user/role controllers query or mutate users and roles
- **THEN** they use inbound ports backed by UserCatalogPort / RoleCatalogPort (domain types only)

#### Scenario: ArchUnit R2 baseline shrinks
- **GIVEN** two R2 baseline edges for UserRepositoryPort and RoleRepositoryPort
- **WHEN** HexagonArchitectureTest runs after the change
- **THEN** those edges are gone and no new R2 violations appear
