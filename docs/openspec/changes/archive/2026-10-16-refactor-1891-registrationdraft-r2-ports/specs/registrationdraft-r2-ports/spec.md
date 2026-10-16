## ADDED Requirements

### Requirement: Registrationdraft application ports stay entity-free

The registrationdraft application slice MUST NOT depend on JPA entities or Spring Data repositories. Entity orchestration lives in outbound persistence adapters behind view-safe ops ports.

#### Scenario: OpsPort returns DTO views only
- **GIVEN** a registration draft query or workflow mutation
- **WHEN** inbound adapters call RegistrationDraftOpsPort
- **THEN** results are DtoRegistrationDraft sealed views with no entity types on the port

#### Scenario: Entity helper lives in outbound adapter
- **GIVEN** RegistrationDraftService needs Deed/Procedure/Property and Spring Data repositories
- **WHEN** the R2 shrink lands
- **THEN** the helper is under adapter.out.persistence.registrationdraft and application.usecase.registrationdraft no longer imports business entities or repositories

#### Scenario: Entity RepositoryPort removed
- **GIVEN** RegistrationDraftRepositoryPort exposed RegistrationDraft entities
- **WHEN** the R2 shrink lands
- **THEN** that port and its unused PersistenceAdapter are deleted

#### Scenario: ArchUnit R2 baseline shrinks
- **GIVEN** eight R2 baseline edges for registrationdraft
- **WHEN** HexagonArchitectureTest runs after the move
- **THEN** those edges are gone and no new R2 violations appear