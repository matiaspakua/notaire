# quiet-optional-otel Specification

## Purpose
Keep optional OTel exporters and Hibernate Temporal deprecation from flooding ERROR/WARN when the collector is absent or entities still use `@Temporal`. Source: #1736; owner CU77.

## Requirements

### Requirement: OTLP export is opt-in

The backend SHALL leave Micrometer OTLP metrics export and OTLP span export disabled by default. Docker Compose (or an operator) SHALL set the enable flags when the collector is present. Actuator Prometheus scrape remains available either way.

#### Scenario: Default properties disable OTLP export

- **WHEN** `application.properties` is read without OTLP enable overrides
- **THEN** `management.otlp.metrics.export.enabled` and `management.tracing.export.otlp.enabled` default to false

#### Scenario: Compose stack enables OTLP export

- **WHEN** the app is started via root `docker-compose.yml` with the observability network
- **THEN** the backend environment sets OTLP metrics and tracing export enabled with the collector URLs

### Requirement: Hibernate Temporal deprecation is not WARN spam

The backend SHALL configure logging so Hibernate `@Temporal` deprecation messages are not emitted at WARN during normal traffic.

#### Scenario: Deprecation logger is above WARN

- **WHEN** `application.properties` is read
- **THEN** `logging.level.org.hibernate.orm.deprecation` is ERROR or higher
