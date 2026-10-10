# playwright-shards

The Playwright UI E2E job runs as a shard matrix with a fail-closed merged report.

## ADDED Requirements

### Requirement: E2E job uses a three-shard matrix

The Playwright workflow MUST run UI E2E tests with `--shard=${{ matrix.shard }}/3`
(or equivalent) across three shards.

#### Scenario: Matrix defined

- **WHEN** `playwright-e2e.yml` is inspected
- **THEN** the UI E2E job defines a matrix with three shard values

### Requirement: Merge job fails if any shard failed

A merge/report job MUST aggregate shard results and fail when any shard failed or is missing.

#### Scenario: Shard failure propagates

- **WHEN** any E2E shard concludes `failure`
- **THEN** the merge job and Playwright suite aggregator do not succeed

### Requirement: Path-scoped skip still applies

When #1257 path filters mark `product` false, the sharded E2E stack MUST remain skipped
(aggregators accept intentional skipped).

#### Scenario: Docs-only PR

- **WHEN** a docs-only PR runs
- **THEN** sharded E2E leaf jobs are skipped
