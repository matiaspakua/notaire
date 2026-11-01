# legacy-jpa-retirement Specification

## Purpose
TBD - created by archiving change chore-576-retire-legacy-jpa. Update Purpose after archive.
## Requirements
### Requirement: Legacy jpa package and JpaAdministrator are gone

The production classpath MUST NOT contain `com.licensis.notaire.jpa` types or
`com.licensis.notaire.service.JpaAdministrator` / `LegacyJpaEmfConfig`. Catalog
bootstrap for `IdentificationTypeLookup` MUST use Spring Data
`IdentificationTypeRepository` instead of `IdentificationTypeJpaController`.

#### Scenario: PersonJpaController class is absent
- **GIVEN** the backend-api main sources after this change
- **WHEN** code attempts `Class.forName("com.licensis.notaire.jpa.PersonJpaController")`
- **THEN** a `ClassNotFoundException` is thrown

#### Scenario: JpaAdministrator class is absent
- **GIVEN** the backend-api main sources after this change
- **WHEN** code attempts `Class.forName("com.licensis.notaire.service.JpaAdministrator")`
- **THEN** a `ClassNotFoundException` is thrown

#### Scenario: LegacyJpaEmfConfig class is absent
- **GIVEN** the backend-api main sources after this change
- **WHEN** code attempts `Class.forName("com.licensis.notaire.service.LegacyJpaEmfConfig")`
- **THEN** a `ClassNotFoundException` is thrown

#### Scenario: IdentificationTypeLookup wires from repository supplier
- **GIVEN** a catalog supplier that returns one identification type
- **WHEN** `IdentificationTypeLookup.wireCatalog` is invoked with that supplier
- **THEN** `shared().nameOf(id)` and `shared().idOf(name)` resolve that type

#### Scenario: ArchUnit R1–R6 stay empty
- **GIVEN** `hexagon-baseline.txt` is empty
- **WHEN** `HexagonArchitectureTest` runs after the deletion
- **THEN** no new R1–R6 violations are recorded

