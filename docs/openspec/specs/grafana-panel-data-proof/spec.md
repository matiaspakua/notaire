# grafana-panel-data-proof Specification

## Purpose
TBD - created by archiving change test-1735-grafana-panel-proof. Update Purpose after archive.

## Requirements

### Requirement: Panel inventory is wired to provisioned dashboards

The repository SHALL maintain an inventory of (dashboard uid, panel label, PromQL) for `notaire-status`, `notaire-postgres`, `notaire-backend`, and `notaire-quality`, and each PromQL SHALL appear in that dashboard JSON.

#### Scenario: Wiring test covers required dashboards

- **WHEN** `infra/tests/test_grafana_panel_proof.py` runs
- **THEN** every inventory PromQL is present in the matching provisioned dashboard

### Requirement: Live script asserts non-empty panel series

`infra/scripts/verify_grafana_panels.py` SHALL query Prometheus for inventory PromQL and Grafana for dashboard UIDs, exiting non-zero on FAIL (SKIP allowed for optional Sonar).

#### Scenario: Operator can run panel proof after traffic

- **WHEN** an operator runs `bash infra/scripts/verify-signals.sh --with-grafana-panels` (optionally after `--with-e2e`)
- **THEN** the script executes the panel proof after signal checks
