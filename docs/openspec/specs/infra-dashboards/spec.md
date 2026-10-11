# infra-dashboards Specification

## Purpose
Give operators drill-down views for host resources, the frontend, the observability tools and traces.
## Requirements
### Requirement: Observability tools are scraped

Prometheus SHALL scrape Tempo, Alertmanager and Promtail, and the collector SHALL expose its own metrics.

#### Scenario: Tool targets exist

- **WHEN** the Prometheus configuration is read
- **THEN** jobs for tempo, alertmanager and promtail exist with stable `service` labels

### Requirement: Drill-down dashboards

Grafana SHALL provision `notaire-infra-resources`, `notaire-frontend`, `notaire-observability-health` and
`notaire-traces`, each using the provisioned datasources, linking to the other dashboards, and rendering a no-data
state instead of failing when a metric is absent.

#### Scenario: Dashboards are provisioned and safe

- **WHEN** the dashboard files are inspected
- **THEN** each has its UID, only provisioned datasource UIDs, links to the others and no-data settings on value panels

#### Scenario: Queries return data on the live stack

- **WHEN** every panel query is run against the live datasources
- **THEN** none errors

### Requirement: Existing dashboards are preserved

The backend, PostgreSQL, authentication, logs and the two overview dashboards SHALL remain provisioned.

#### Scenario: Existing UIDs intact

- **WHEN** the provisioned dashboards are listed
- **THEN** all earlier UIDs are still present

