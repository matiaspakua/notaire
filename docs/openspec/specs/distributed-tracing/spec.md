# distributed-tracing Specification

## Purpose
Let operators follow a Notaire request as a trace.
## Requirements
### Requirement: Tempo stores traces

The stack SHALL run Tempo, receiving traces from the OTel Collector.

#### Scenario: Collector forwards traces

- **WHEN** the collector receives OTLP traces
- **THEN** it forwards them to Tempo through a `traces` pipeline

### Requirement: Backend exports traces

The backend SHALL create OpenTelemetry spans for HTTP requests and export them over OTLP to the collector.
Export failures MUST NOT affect request handling.

#### Scenario: A request produces a trace

- **WHEN** a request is served by the backend
- **THEN** its trace can be fetched from Tempo by id

### Requirement: Grafana explores traces

Grafana SHALL provision a Tempo datasource linked to Loki for logs.

#### Scenario: Datasource is provisioned

- **WHEN** Grafana starts
- **THEN** a datasource with uid `tempo` exists and links traces to Loki

