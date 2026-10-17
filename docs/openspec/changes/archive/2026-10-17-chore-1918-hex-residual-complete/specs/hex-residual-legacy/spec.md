## ADDED Requirements

### Requirement: Residual legacy packages shrink safely after ArchUnit empty

After ArchUnit R1–R6 baseline is empty, safe residual cleanup MUST remove dead
`service` types and relocate `PersistenceConstants` outside `jpa`, without changing
runtime behaviour. Full `*JpaController` retirement remains on #576.

#### Scenario: PersistenceConstants lives outside jpa
- **GIVEN** Folio and Notebook imported `com.licensis.notaire.jpa.PersistenceConstants`
- **WHEN** the residual slice lands
- **THEN** they use `com.licensis.notaire.persistence.PersistenceConstants` and
  `jpa.PersistenceConstants` is gone

#### Scenario: Dead SessionAdministrator removed
- **GIVEN** SessionAdministrator has no callers outside its own file
- **WHEN** the residual slice lands
- **THEN** the class file is deleted

#### Scenario: Dead ValidationAdministrator removed
- **GIVEN** ValidationAdministrator has no callers outside its own file
- **WHEN** the residual slice lands
- **THEN** the class file is deleted

#### Scenario: ArchUnit R1–R6 stay empty
- **GIVEN** hexagon-baseline.txt is empty
- **WHEN** HexagonArchitectureTest runs after the change
- **THEN** no new R1–R6 edges appear
