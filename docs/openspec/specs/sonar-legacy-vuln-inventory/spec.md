# sonar-legacy-vuln-inventory Specification

## Purpose
In-repo export and classification of overall (legacy) SonarQube vulnerability and high-severity findings for Notaire projects (CU78 / #1535 slice 1).

## Requirements

### Requirement: Legacy inventory export script

The repository SHALL provide a Python script under `infra/scripts/` that queries Sonar for unresolved issues on `notaire-backend` and `notaire-frontend` and emits a machine-parseable or markdown inventory including at least: project, rule key, severity, type, component, message key or short message, and a classification placeholder.

#### Scenario: Exporter lists vulnerability-type issues

- **WHEN** the inventory script is run against a Sonar fixture or live Sonar with unresolved `VULNERABILITY` issues
- **THEN** each such issue appears as a row with project, rule, severity, type, and component

#### Scenario: Exporter is unit-tested without live Sonar

- **WHEN** CI runs the inventory script tests
- **THEN** tests use fixture JSON and do not require a reachable Sonar host

### Requirement: Checked-in classification inventory

The repository SHALL keep a markdown inventory under `docs/300-development/` that classifies every listed vulnerability row as one of: `fix`, `false_positive`, `accepted_risk`, `defer_hexagon`, with an English rationale.

#### Scenario: Every vulnerability row is classified

- **WHEN** a reviewer reads the inventory after slice 1
- **THEN** no vulnerability row has an empty classification cell
