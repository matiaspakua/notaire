# prometheus-live-check Specification

## Purpose
TBD - created by archiving change test-1666-prometheus-live. Update Purpose after archive.

## Requirements

### Requirement: Prometheus is checked against a live backend without Docker

When a `prometheus` binary and a running backend exist, a test MUST start Prometheus with the repository's scrape configuration, and MUST fail unless the backend target is up, every alert rule is healthy and the metric families the alerts rely on return series. Without the binary or the backend the test MUST skip. The cloud toolchain hook MUST install `prometheus` at the version of the compose image.

#### Scenario: the backend target is scraped

- **WHEN** Prometheus runs with the repository configuration against the live backend
- **THEN** the notaire-backend target reports up

#### Scenario: every alert rule is healthy

- **WHEN** the rule groups are read from Prometheus
- **THEN** each rule has health ok

#### Scenario: alert metric families exist

- **WHEN** the HTTP, connection pool and JVM metric families are queried
- **THEN** each returns at least one series

#### Scenario: the hook installs prometheus at the image version

- **WHEN** the hook pin is compared with the compose image tag
- **THEN** it is equal
