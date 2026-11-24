## ADDED Requirements

### Requirement: ArchUnit guards every legacy package

`HexagonArchitectureTest` SHALL forbid the legacy web DTO package `com.licensis.notaire.dto` in the application
layer (R2) and SHALL report every dependency on the legacy `jpa`, `service`, `repository`, `business` and `dto`
packages from outside them (R6). Existing dependencies are recorded once in the shrink-only baseline.

#### Scenario: A new port on a legacy DTO fails
- **WHEN** a class in `application.*` imports a class from `com.licensis.notaire.dto` that is not in the baseline
- **THEN** `HexagonArchitectureTest` fails with an R2 new violation

#### Scenario: A new dependent of the legacy JPA model fails
- **WHEN** a class outside the legacy packages starts using a class from `business` or `repository` that is not in the baseline
- **THEN** `HexagonArchitectureTest` fails with an R6 new violation

#### Scenario: Fixing a recorded dependency shrinks the baseline
- **WHEN** a recorded dependency disappears
- **THEN** the test fails until `-Darchitecture.baseline=shrink` removes the line
