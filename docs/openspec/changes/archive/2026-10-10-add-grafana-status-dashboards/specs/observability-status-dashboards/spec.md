## Purpose

Provide a dependable first operational view of Notaire and its supporting
technology stack so incident responders can see availability, alerts, and
recent errors before moving to a specialist diagnostic dashboard.

## ADDED Requirements

### Requirement: Technology stack availability signals

The observability stack SHALL expose a Prometheus availability signal for every
running Notaire application service and every operational dependency that has a
network health endpoint or a database port. Each signal MUST identify the
service by a stable label and return `0` when the service is unreachable. This
requirement implements the availability-monitoring responsibility of CU77.

#### Scenario: All configured services are reachable

- **WHEN** each configured Notaire and observability service responds to its
  health endpoint or database port
- **THEN** Prometheus records an availability signal of `1` for each service
  with its stable service label

#### Scenario: A monitored service is unavailable

- **WHEN** a configured service cannot respond to its health endpoint or
  database port
- **THEN** Prometheus records an availability signal of `0` for that service
  without suppressing the status of the remaining services

### Requirement: Technology stack overview dashboard

Grafana SHALL provision a technology-stack overview dashboard that displays the
availability of all monitored Notaire and observability services, active
Prometheus alerts, and recent error logs from the stack. The dashboard MUST use
the provisioned Prometheus and Loki data sources and MUST render a no-data or
unavailable state rather than an invalid query when telemetry is absent.

#### Scenario: Operator assesses a healthy technology stack

- **WHEN** an operator opens the technology-stack overview while all monitored
  services are available and no alert is firing
- **THEN** the dashboard displays each service as available, no active alerts,
  and the recent error-log panel remains queryable

#### Scenario: Operator identifies an infrastructure incident

- **WHEN** an availability signal is `0`, an alert is active, or an error log
  is present
- **THEN** the dashboard identifies the affected service or alert and displays
  the recent error evidence without hiding other service states

#### Scenario: Optional telemetry is absent

- **WHEN** Loki has no matching log lines or a Prometheus metric has not yet
  been emitted
- **THEN** the dashboard shows an empty or no-data panel and remains usable

### Requirement: Notaire service status dashboard

Grafana SHALL provision a Notaire status dashboard that summarizes frontend,
backend API, PostgreSQL, and pgAdmin availability, active Notaire alerts, and
recent Notaire application errors. It MUST provide a concise operational view
and direct operators to existing backend, PostgreSQL, authentication, and log
dashboards for detailed diagnosis.

#### Scenario: Operator assesses Notaire readiness

- **WHEN** an operator opens the Notaire status dashboard while the frontend,
  backend API, PostgreSQL, and pgAdmin are available
- **THEN** the dashboard shows each service as available and exposes the current
  Notaire alert and error-log state

#### Scenario: A Notaire dependency becomes unavailable

- **WHEN** any monitored Notaire service is unavailable
- **THEN** the dashboard visibly identifies that service as unavailable while
  retaining the status of the other Notaire services

### Requirement: Specialist dashboard preservation

The overview dashboards SHALL complement, not replace, the existing backend,
PostgreSQL, authentication, and logs dashboards. The operational documentation
MUST describe the overview-first and specialist-drill-down workflow required by
CU77.

#### Scenario: Operator needs detailed diagnosis

- **WHEN** an overview panel identifies a backend, database, authentication, or
  log concern
- **THEN** the operator can identify the corresponding existing specialist
  dashboard from the overview or the documented triage workflow
