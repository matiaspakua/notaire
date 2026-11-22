# factory-store-telemetry Specification

## Purpose

TBD - created by archiving change feat-2394-factory-store-telemetry. Update Purpose after archive.

## Requirements

### Requirement: Store schema and idempotent migrations

The system SHALL create a SQLite database at `.aisdlc/factory.db` with tables
`events` (ts, kind, run_id, intent_id, stage, step, actor, data), `intents`,
`telemetry` (ts, intent_id, run_id, stage, step, agent, model, skill, tool,
metric, value, ok), `benchmarks` (name, ts, data), `config_snapshots` (ts,
digest, files), `sync_keys` (source, hash), and a `schema_migrations` version table.
The file SHALL be git-ignored via `.aisdlc/.gitignore`. Schema migration SHALL be
idempotent — calling it repeatedly produces the same schema without error.

#### Scenario: first migration creates all tables

- **WHEN** the store is opened on a fresh directory with no `.aisdlc/factory.db`
- **THEN** the database is created with all six data tables and `schema_migrations`,
  and the migration version is recorded

#### Scenario: re-running migration is a no-op

- **WHEN** `migrate()` is called on an already-migrated database
- **THEN** no tables are altered, no error is raised, and the version remains unchanged

#### Scenario: store file is git-ignored

- **WHEN** the store is initialized
- **THEN** `.aisdlc/.gitignore` contains `factory.db`

### Requirement: Sync idempotency

The system SHALL import evidence ledger rows, cost log entries, and RepoLens
`.repolens/usage.jsonl` lines into the store idempotently. The idempotency key
SHALL be the combination of the source label (`ledger`, `cost`, `repolens`) and
the SHA-256 hash of the raw JSONL line. Re-running sync SHALL NOT create duplicate
rows. RepoLens `input` (query text) and `files` keys SHALL NEVER be stored.

#### Scenario: sync imports ledger rows

- **WHEN** the evidence ledger contains two stage-run records and sync is executed
- **THEN** the `events` table contains two rows with the corresponding data

#### Scenario: sync is idempotent — second run adds no rows

- **WHEN** sync is executed twice against the same evidence files
- **THEN** the row count after the second run equals the count after the first run

#### Scenario: sync imports cost log entries

- **WHEN** the cost log contains three cost entries and sync is executed
- **THEN** the `telemetry` table contains rows for those cost entries with
  metric `usd` and the correct value

#### Scenario: sync reads RepoLens usage defensively

- **WHEN** `.repolens/usage.jsonl` contains lines with keys `ts`, `src`, `client`,
  `tool`, `input`, `ms`, `bytes`, `tokens`, `results`, `files`
- **THEN** the store records `tool`, `ms`, `tokens`, `results`, `client`, `src`
  but never stores `input` or `files` values

#### Scenario: RepoLens line with missing keys is handled

- **WHEN** a RepoLens usage line is missing optional keys (e.g., `tokens` or `results`)
- **THEN** the sync does not crash and stores the available fields with null defaults

### Requirement: Telemetry record and report math

The system SHALL provide `aisdlc telemetry record` to insert a telemetry row
(intent, run, stage, step, agent, model, skill, tool, metric, value, ok) and
`aisdlc telemetry report --group intent|stage|agent|skill|tool --json` to return
aggregated time, cost, and failure counts per group key. The report SHALL include
RepoLens call counts and latency when available.

#### Scenario: record a telemetry row

- **WHEN** `aisdlc telemetry record` is called with stage, agent, metric, and value
- **THEN** a row is inserted in the `telemetry` table with those fields

#### Scenario: report groups by stage with correct sums

- **WHEN** the telemetry table has rows for stages `pre-push` (2 runs, 5.0s total,
  $0.01, 1 failure) and `pre-merge` (1 run, 3.0s, $0.005, 0 failures)
- **THEN** `aisdlc telemetry report --group stage --json` returns two rows with
  correct duration_s, usd, and failures per stage

#### Scenario: report groups by agent with failure counts

- **WHEN** the telemetry table has rows for agents `junie` (3 runs, 2 failures)
  and `cursor` (1 run, 0 failures)
- **THEN** the report grouped by agent shows the correct failure counts

#### Scenario: store stats returns row counts

- **WHEN** the store has rows in events, telemetry, and benchmarks tables
- **THEN** `aisdlc store stats --json` returns the count per table

### Requirement: No secrets in rows

The system SHALL redact prompt text, token values, and secret-shaped keys from
every telemetry row before insertion, using `telemetry.redact_attributes`. No row
in any store table SHALL contain a value matching the banned-key pattern
(prompt, secret, password, token, authorization, api_key, private_key, credential).

#### Scenario: record with a secret-shaped key is redacted

- **WHEN** `aisdlc telemetry record` is called with an attribute key `api_key`
- **THEN** the row is inserted without that key

#### Scenario: no secret value exists in any telemetry row after sync

- **WHEN** the evidence ledger contains a record with a field named `secret_token`
  and sync imports it
- **THEN** the stored row does not contain the `secret_token` key or its value

### Requirement: Absent store gives empty report

The system SHALL return an empty report (zero rows, zero totals) when the store
database does not exist, rather than raising an error.

#### Scenario: report with no store file

- **WHEN** `.aisdlc/factory.db` does not exist and `aisdlc telemetry report --group stage --json` is called
- **THEN** the command returns `{"group": "stage", "rows": [], "total_runs": 0,
  "total_duration_s": 0, "total_usd": 0, "total_failures": 0}` with exit code 0

#### Scenario: stats with no store file

- **WHEN** `.aisdlc/factory.db` does not exist and `aisdlc store stats --json` is called
- **THEN** the command returns `{"events": 0, "telemetry": 0, "benchmarks": 0,
  "config_snapshots": 0, "intents": 0}` with exit code 0

### Requirement: GET /factory/telemetry read scope

The system SHALL expose `GET /factory/telemetry?group=intent|stage|agent|skill|tool`
on the `aisdlc serve` HTTP host, requiring only read scope authentication. The
response SHALL match the JSON shape of `aisdlc telemetry report --json`.

#### Scenario: telemetry endpoint returns report with read token

- **WHEN** a client sends `GET /factory/telemetry?group=stage` with a valid
  read-scope Bearer token
- **THEN** the response is 200 with the telemetry report JSON

#### Scenario: telemetry endpoint rejects unauthenticated request

- **WHEN** a client sends `GET /factory/telemetry` without a Bearer token
- **THEN** the response is 401 Unauthorized

#### Scenario: telemetry endpoint rejects invalid group

- **WHEN** a client sends `GET /factory/telemetry?group=invalid` with a valid token
- **THEN** the response is 400 with an error message listing valid groups

#### Scenario: report includes failure counts by integration

- **WHEN** the store has harness events with `ok=false` from adapters `junie` (2
  failures) and `repolens` (1 failure)
- **THEN** the report JSON contains `failures_by_integration: {"junie": 2, "repolens": 1}`

#### Scenario: report includes repolens summary

- **WHEN** the store has RepoLens sync rows with tools `search` (3 calls, 120 ms total)
  and `node` (2 calls, 80 ms total)
- **THEN** the report JSON contains `repolens: {calls: 5, total_ms: 200, avg_ms: 40,
  by_tool: {"search": 3, "node": 2}}`

#### Scenario: GET /factory/telemetry matches CLI report

- **WHEN** a client sends `GET /factory/telemetry?group=stage` with a valid read token
- **THEN** the response body is the same JSON shape as `aisdlc telemetry report --group stage --json`
  including `failures_by_integration` and `repolens` fields
