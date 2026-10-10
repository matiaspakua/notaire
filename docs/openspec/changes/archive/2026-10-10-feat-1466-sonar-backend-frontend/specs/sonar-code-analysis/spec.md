## Purpose

Measure code quality and security on both Notaire modules with one repeatable command.

## ADDED Requirements

### Requirement: Backend analysis

The backend SHALL be analyzed as SonarQube project `notaire-backend` with JaCoCo coverage and
Surefire test results, excluding the legacy `jpa` package consistently with the JaCoCo gate.

#### Scenario: Backend analysis is submitted

- **WHEN** the analysis command runs against a started SonarQube
- **THEN** project `notaire-backend` exists with coverage and test measures

### Requirement: Frontend analysis

The frontend SHALL be analyzed as project `notaire-frontend` with Vitest lcov coverage and the ESLint
report imported as external issues.

#### Scenario: Frontend analysis is submitted

- **WHEN** the analysis command runs
- **THEN** project `notaire-frontend` exists with coverage and imported lint issues

### Requirement: Quality gate

Both projects SHALL use a "Notaire way" quality gate, and the command MUST exit non-zero when a gate fails.

#### Scenario: Gate passes

- **WHEN** a project meets the gate conditions
- **THEN** the command reports the gate as passed and exits zero

#### Scenario: Gate fails

- **WHEN** a project violates a gate condition
- **THEN** the command prints the failed condition and exits non-zero

### Requirement: Configuration is versioned

Project settings SHALL live in the repository (`pom.xml`, `sonar-project.properties`, script) and
MUST NOT contain credentials.

#### Scenario: No secret in configuration

- **WHEN** the configuration files are inspected
- **THEN** none contains a token or password
