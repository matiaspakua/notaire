# history-r2-ports Specification

## Purpose
TBD - created by archiving change refactor-1892-history-r2-ports. Update Purpose after archive.

## Requirements

### Requirement: History application ports stay entity-free

The history application slice MUST NOT expose JPA entities on inbound or outbound ports. Entity mapping lives in outbound persistence adapters behind view-safe ops ports.

#### Scenario: Dead entity ports removed
- **GIVEN** HistoryUseCase and HistoryRepositoryPort speak History entities and have no live consumers beyond the unused PersistenceAdapter
- **WHEN** the R2 shrink lands
- **THEN** those ports and HistoryPersistenceAdapter are deleted

#### Scenario: Mapper lives in outbound adapter
- **GIVEN** HistoryMapper maps History/DeedManagement/ManagementStatus entities to DtoHistorySummary
- **WHEN** the R2 shrink lands
- **THEN** HistoryMapper is under adapter.out.persistence.history and application.usecase.history no longer imports business entities via the mapper

#### Scenario: OpsPort remains view-safe
- **WHEN** HistoryController queries or mutates history
- **THEN** it uses QueryHistoriesUseCase / ManageHistoriesUseCase backed by HistoryOpsPort (DTO views only)

#### Scenario: ArchUnit R2 baseline shrinks
- **GIVEN** five R2 baseline edges for history
- **WHEN** HexagonArchitectureTest runs after the change
- **THEN** those edges are gone and no new R2 violations appear
