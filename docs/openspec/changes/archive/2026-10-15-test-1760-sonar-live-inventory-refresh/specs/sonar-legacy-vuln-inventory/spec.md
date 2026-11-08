## MODIFIED Requirements

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
