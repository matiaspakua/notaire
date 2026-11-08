## Purpose

Wire observability dashboards so Postgres, quality, and release-identity panels show real signals instead of empty/misconfigured queries.

## ADDED Requirements

### Requirement: Postgres exporter loads Notaire custom queries

The observability compose stack SHALL set PG_EXPORTER_EXTEND_QUERY_PATH (or --extend.query-path) to the mounted queries file, and that file SHALL use the exporter extend-query metric-key format so deeds/budgets/users/people and connection-state gauges are scraped.

#### Scenario: Extend path is configured

- **WHEN** infra/observability/docker-compose.yml is read for postgres-exporter
- **THEN** PG_EXPORTER_EXTEND_QUERY_PATH points at /etc/postgres_exporter/queries.yml

#### Scenario: Queries file is extend-query format

- **WHEN** postgres_exporter.yml is parsed
- **THEN** it has no custom_queries wrapper and defines deeds_total, budgets_total, users_total, and people_total metric keys

### Requirement: Postgres dashboard PromQL matches exported series

notaire-postgres SHALL use pg_stat_database_blks_* for cache hit ratio and pg_connections_total without a datname selector that the extend-query gauge does not carry.

#### Scenario: Cache hit ratio

- **WHEN** the Cache Hit Ratio panel expr is inspected
- **THEN** it references pg_stat_database_blks_hit and pg_stat_database_blks_read, not bare blks_hit/blks_read

### Requirement: Packaged SEMVER is visible on metrics and dashboards

The backend SHALL publish Micrometer tag version=${app.version}. Overview and backend Grafana dashboards SHALL include a Product version panel over that tag.

#### Scenario: Tag and panels

- **WHEN** application.properties and notaire-status / notaire-backend are inspected
- **THEN** management.metrics.tags.version is set and both dashboards query version!=""

### Requirement: Sonar quality dashboard as code

Grafana SHALL provision notaire-quality with SonarQube and sonar-db probe panels, noValue-safe stats, and operator load instructions; Homer SHALL link /d/notaire-quality.

#### Scenario: Dashboard and Homer

- **WHEN** provisioning and Homer config are read
- **THEN** notaire-quality.json exists with probe_success for sonarqube and sonar-db, and Homer URLs include /d/notaire-quality

### Requirement: No deprecated piechart plugin install

Grafana compose SHALL NOT set GF_INSTALL_PLUGINS=grafana-piechart-panel.

#### Scenario: Compose environment

- **WHEN** the grafana service environment is read
- **THEN** grafana-piechart-panel is absent
