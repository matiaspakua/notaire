## ADDED Requirements

### Requirement: Extend-query metric names match Grafana PromQL

The postgres-exporter extend-query file SHALL emit series named `pg_connections_total`, `deeds_total`, `budgets_total`, `people_total`, and `users_total` (no doubled group_column prefix).

#### Scenario: Connection and business gauges avoid double-prefix naming

- **WHEN** `infra/observability/prometheus/postgres_exporter.yml` is loaded
- **THEN** each extend-query group uses column alias `total` under a group key that is the PromQL base without a duplicated suffix, so the scraped name equals the Grafana target

### Requirement: Exporter role can SELECT business tables

Flyway SHALL grant the postgres-exporter role SELECT on `deeds`, `budgets`, `people`, and `users` so business gauges scrape without permission denied.

#### Scenario: Migration grants least-privilege SELECT

- **WHEN** V51 runs
- **THEN** `${exporterUsername}` has SELECT on the four business tables and R51 can revoke those grants

### Requirement: Operations rate panel uses an existing metric

The notaire-postgres Database Operations Rate panel SHALL reference a metric that exists on tip (not `notaire_database_operation_duration_sum`).

#### Scenario: Ops-rate PromQL uses notaire_operation_total

- **WHEN** `notaire-postgres.json` is inspected
- **THEN** it does not reference `notaire_database_operation_duration` and uses `notaire_operation_total` instead
