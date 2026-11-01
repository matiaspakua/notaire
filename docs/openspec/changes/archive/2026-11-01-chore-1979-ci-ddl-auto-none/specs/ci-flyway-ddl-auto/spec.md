## ADDED Requirements

### Requirement: CI full-stack jobs use Flyway-only ddl-auto

Workflows that start the Notaire backend for E2E, DAST or performance MUST set
`spring.jpa.hibernate.ddl-auto` to `none` or `validate`, and MUST NOT set `update`.

#### Scenario: Guarding workflows reject ddl-auto=update
- **GIVEN** `dast-zap.yml`, `performance-test.yml` and `playwright-e2e.yml`
- **WHEN** `CiDdlAutoNoneTest` runs
- **THEN** none of those files contain `ddl-auto=update`

#### Scenario: Guarding workflows pin none or validate
- **GIVEN** the same three workflow files
- **WHEN** `CiDdlAutoNoneTest` runs
- **THEN** each file contains `ddl-auto=none` or `ddl-auto=validate`
