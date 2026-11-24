## ADDED Requirements

### Requirement: ArchUnit guards every legacy package

`LegacyPackageArchitectureTest` (rule R8) SHALL report every dependency from outside the legacy packages on
`com.licensis.notaire.repository`, `.business` or `.dto`, which covers ports and use cases that speak legacy web
DTOs. Existing dependencies are recorded once in `legacy-dependents-baseline.txt`, a new shrink-only baseline
registered as an `aisdlc` ratchet.

#### Scenario: A new port on a legacy DTO fails
- **WHEN** a class in `application.*` imports a class from `com.licensis.notaire.dto` that is not in the baseline
- **THEN** `LegacyPackageArchitectureTest` fails with an R8 new violation

#### Scenario: A new dependent of the legacy JPA model fails
- **WHEN** a class outside the legacy packages starts using a class from `business` or `repository` that is not in the baseline
- **THEN** `LegacyPackageArchitectureTest` fails with an R8 new violation

#### Scenario: Fixing a recorded dependency shrinks the baseline
- **WHEN** a recorded dependency disappears
- **THEN** the test fails until `-Darchitecture.baseline=shrink` removes the line, and `aisdlc ratchet` refuses any added line
