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

The repository SHALL keep a markdown inventory under `docs/300-development/` that classifies every listed vulnerability row as one of: `fix`, `false_positive`, `accepted_risk`, `defer_hexagon`, with an English rationale. When refreshed from a reachable Sonar host, the inventory SHALL record a live-refresh provenance note and SHALL NOT rely solely on seed residuals when live unresolved findings exist.

#### Scenario: Every vulnerability row is classified

- **WHEN** a reviewer reads the inventory after a live refresh
- **THEN** no vulnerability row has an empty classification cell

#### Scenario: Live refresh records provenance

- **WHEN** the inventory script writes markdown after a successful Sonar query
- **THEN** the inventory includes a `Last live refresh` note naming the Sonar source

#### Scenario: Known live vulnerability clusters get stable heuristics

- **WHEN** live Sonar returns `secrets:S6703` on `.github/workflows/`, `java:S4790` on `UserController`, or `typescript:S5332` on `otlp-proxy.ts`
- **THEN** the exporter applies the documented English classification heuristics without leaving those rows unclassified

