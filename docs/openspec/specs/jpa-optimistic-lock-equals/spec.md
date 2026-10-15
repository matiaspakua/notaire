# jpa-optimistic-lock-equals Specification

## Purpose
TBD - created by archiving change fix-1758-jpa-s4973-equals. Update Purpose after archive.

## Requirements

### Requirement: Legacy JpaControllers compare versions with equals

Optimistic-lock version checks in `*JpaController` MUST compare boxed Integers with value equality (`Objects.equals` / `OptimisticLockVersions.differ`), never with `==` or `!=`. A unit test MUST fail if the forbidden `version != oldVersion` pattern returns. Inventory S4973 rows for these controllers MUST be classified `fix` (not `defer_hexagon`).

#### Scenario: value equality for distinct Integer instances

- **WHEN** two Integer instances hold the same numeric version
- **THEN** OptimisticLockVersions.differ reports they do not differ

#### Scenario: source scan finds no reference inequality

- **WHEN** JpaOptimisticLockEqualsTest scans jpa *JpaController sources
- **THEN** no line contains `version != oldVersion`

#### Scenario: inventory no longer defers S4973 for jpa equality

- **WHEN** SONAR-LEGACY-VULN-INVENTORY is read for java:S4973 on JpaControllers
- **THEN** those rows are classified `fix` with rationale pointing at #1758
