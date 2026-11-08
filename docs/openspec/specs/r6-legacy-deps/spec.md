# r6-legacy-deps Specification

## Purpose
ArchUnit R6 clear: non-legacy packages do not depend on jpa/service; baseline R1–R6 empty (#1914 / ADR-030).

## Requirements

### Requirement: Non-legacy packages do not depend on jpa or service

Types outside `com.licensis.notaire.jpa` and `com.licensis.notaire.service` MUST NOT depend on those legacy packages.

#### Scenario: IdentificationTypeLookup has no jpa import
- **GIVEN** IdentificationTypeLookup previously referenced IdentificationTypeJpaController
- **WHEN** the R6 clear lands
- **THEN** the lookup uses a wired catalog supplier and does not import jpa.*

#### Scenario: EMF bootstrap lives under service
- **GIVEN** config.JpaConfig depended on JpaAdministrator
- **WHEN** the R6 clear lands
- **THEN** bootstrap is service.LegacyJpaEmfConfig and config.JpaConfig is deleted

#### Scenario: ArchUnit R6 baseline is empty
- **GIVEN** two R6 baseline edges
- **WHEN** HexagonArchitectureTest runs after the change
- **THEN** no R6 (or R1–R5) edges remain
