## Purpose

Give Prometheus metrics for every major Notaire module and its runtime environment.

## ADDED Requirements

### Requirement: Collector ingests OTLP

The stack SHALL run an OpenTelemetry Collector that accepts OTLP over HTTP and exposes the received
metrics for Prometheus.

#### Scenario: Collector is scraped

- **WHEN** Prometheus scrapes the collector
- **THEN** the target is up and carries the stable label `service="otel-collector"`

### Requirement: Backend exports over OTLP

The backend SHALL export Micrometer metrics over OTLP while keeping the Actuator Prometheus endpoint.
Export failures MUST NOT affect request handling.

#### Scenario: Backend metrics arrive through the collector

- **WHEN** the backend is running and the collector is reachable
- **THEN** backend metrics are present in Prometheus through the collector, and the Actuator scrape still works

### Requirement: Frontend exposes metrics

The frontend SHALL expose Node runtime and HTTP request metrics in Prometheus format at `/api/metrics`.

#### Scenario: Frontend is scraped

- **WHEN** Prometheus scrapes the frontend
- **THEN** the target is up with `service="frontend"` and Node runtime metrics are present

### Requirement: Container and host metrics

The stack SHALL expose container metrics (cAdvisor) and host metrics (node-exporter) to Prometheus.

#### Scenario: Container and host targets are up

- **WHEN** Prometheus scrapes both exporters
- **THEN** both targets are up with stable `service` labels
