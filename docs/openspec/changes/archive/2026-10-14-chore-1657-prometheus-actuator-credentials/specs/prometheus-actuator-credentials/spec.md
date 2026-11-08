<!-- Governed by CONSTITUTION.md. CU78, issue #1657. -->

## ADDED Requirements

### Requirement: Prometheus has no hardcoded actuator credential

The Prometheus configuration MUST read the actuator scrape credential from files, and the repository MUST NOT contain a literal actuator password in it.

#### Scenario: scrape config uses credential files

- **WHEN** prometheus.yml is parsed
- **THEN** the notaire-backend job uses username_file and password_file and has no literal password

#### Scenario: files come from the environment

- **WHEN** the observability compose file is parsed
- **THEN** the actuator user and password are Compose secrets sourced from ACTUATOR_USER and ACTUATOR_PASSWORD and mounted into prometheus

### Requirement: Example env files ship placeholders

The .env.example files MUST use a change-me placeholder for ACTUATOR_PASSWORD.

#### Scenario: placeholder in both templates

- **WHEN** .env.example and infra/.env.example are read
- **THEN** ACTUATOR_PASSWORD starts with change-me

### Requirement: Dev bootstrap generates a working credential

The bootstrap helper MUST replace a missing or placeholder ACTUATOR_PASSWORD with a random value and MUST NOT touch a real one.

#### Scenario: placeholder replaced

- **WHEN** the helper runs on an env file with ACTUATOR_PASSWORD=change-me-actuator
- **THEN** the file holds a different non-placeholder password

#### Scenario: real value kept

- **WHEN** the helper runs on an env file with a custom ACTUATOR_PASSWORD
- **THEN** the file is unchanged
