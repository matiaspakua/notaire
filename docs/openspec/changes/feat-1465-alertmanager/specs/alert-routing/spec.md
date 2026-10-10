## Purpose

Route, group and expose Notaire alerts.

## ADDED Requirements

### Requirement: Prometheus sends alerts to Alertmanager

Prometheus SHALL send firing alerts to the Alertmanager service.

#### Scenario: Alertmanager is wired

- **WHEN** Prometheus is configured
- **THEN** `alerting.alertmanagers` targets the Alertmanager service and the service is part of the stack

### Requirement: Alert catalogue

The stack SHALL define alerts for service availability, scrape target down, 5xx ratio, API latency, JVM heap,
database connections, connection pool saturation, host memory and disk. Each alert MUST carry `severity`, a
`summary` and a `description`, and the rule files MUST be valid.

#### Scenario: Rules are valid and complete

- **WHEN** the rule files are validated
- **THEN** `promtool` accepts them and every alert has severity, summary and description

### Requirement: Local routing configuration

Alertmanager SHALL group alerts by `alertname` and `service`, inhibit warnings when a critical alert of the same
`service` fires, and use only a local receiver.

#### Scenario: Routing is valid

- **WHEN** the Alertmanager configuration is validated
- **THEN** `amtool` accepts it and it defines grouping and an inhibit rule

### Requirement: Grafana shows Alertmanager

Grafana SHALL provision an Alertmanager datasource.

#### Scenario: Datasource is provisioned

- **WHEN** Grafana starts
- **THEN** a datasource of type `alertmanager` with uid `alertmanager` exists
