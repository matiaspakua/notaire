> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The kit currently records evidence in append-only JSONL files (ledger, cost) and
emits OTel spans through a no-op port. RepoLens tracks usage in `.repolens/usage.jsonl`.
These sources are siloed and session-local. Issue #2393 (Store A) defines the shared
contract for a persistent SQLite store that mirrors these sources and adds a telemetry
CLI and API endpoint. This design implements Store A.

## Goals / Non-Goals

**Goals:**
- Persistent SQLite store at `.aisdlc/factory.db` with versioned schema migrations.
- Idempotent sync from evidence ledger, cost log, and RepoLens usage.
- Telemetry record/report CLI with grouping by intent, stage, agent, skill, tool.
- Dual-write hooks in harness report and run request (fail-open).
- `GET /factory/telemetry` read-only endpoint.
- No secrets, prompts, or RepoLens query text stored in any row.

**Non-Goals:**
- Intent model and CLI (tracked in #2395).
- Production line view (tracked in #2396).
- Live state SSE (future wave).
- Replacing the evidence ledger — the store is a mirror, not the authority.

## Decisions

### SQLite over other stores

**Why SQLite over a file-based alternative?** The contract (#2393) specifies SQLite.
It is stdlib-only (`sqlite3`), supports concurrent reads, and provides SQL for the
report queries. The alternative (a second JSONL file) would require full-file scans
for every report.

### Schema version table with idempotent migrations

**Why a `schema_migrations` table?** The store must survive across kit versions.
A version table (`version INTEGER PRIMARY KEY`) lets `migrate()` apply only the
missing migrations, making it safe to call on every startup. Each migration is a
function that checks `IF NOT EXISTS` before creating/altering tables.

### Idempotency key for sync = source + line hash

**Why source + line hash?** Each JSONL line is immutable once written (hash-chained
for the ledger). Hashing the raw line (SHA-256) gives a stable key: re-syncing the
same line produces the same hash, so `INSERT OR IGNORE` skips it. The source label
(`ledger`, `cost`, `repolens`) disambiguates identical content across files.

### Fail-open dual-write

**Why fail-open?** The store is supplementary — the evidence ledger is the authority.
If the store is locked, corrupt, or absent, the primary operation (harness report,
run request) must not fail. The dual-write hook catches and logs the exception.

### Redact before store

**Why redact at insert time?** The `telemetry.redact_attributes` function already
filters banned keys (prompt, secret, token, etc.). We apply it to every row before
insert. For RepoLens, we additionally drop `input` (query text) and `files` keys
explicitly — they are not covered by the regex but contain sensitive content.

### Report shape

The report returns a JSON object with:
- `group`: the grouping dimension (intent, stage, agent, skill, tool).
- `rows`: array of `{key, runs, duration_s, usd, failures, repolens_calls, repolens_ms}`.
- `total_runs`, `total_duration_s`, `total_usd`, `total_failures`.

## Riesgos / Trade-offs

- [SQLite WAL lock contention] → Mitigation: store is write-on-event, read-on-report;
  concurrent writes are rare (single-agent factory). WAL mode enabled for read/write overlap.
- [Store file grows unbounded] → Mitigation: out of scope for this issue; a future
  issue can add retention/pruning. The sync is idempotent so re-imports don't duplicate.
- [Corrupt store breaks all reads] → Mitigation: `aisdlc store stats` and report
  catch `sqlite3.DatabaseError` and return empty results with a warning.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Store schema creates all tables | unit | `tests/test_store.py::test_schema_creates_tables` |
| Schema migration is idempotent | unit | `tests/test_store.py::test_migration_idempotent` |
| Sync imports ledger rows | unit | `tests/test_telemetry_store.py::test_sync_ledger` |
| Sync is idempotent (no duplicates) | unit | `tests/test_telemetry_store.py::test_sync_idempotent` |
| Sync imports cost log | unit | `tests/test_telemetry_store.py::test_sync_cost` |
| Sync imports RepoLens usage (defensive keys) | unit | `tests/test_telemetry_store.py::test_sync_repolens` |
| RepoLens input and files never stored | unit | `tests/test_telemetry_store.py::test_repolens_no_input_or_files` |
| Telemetry record with redaction | unit | `tests/test_telemetry_store.py::test_record_redacts_secrets` |
| No secret in any row | unit | `tests/test_telemetry_store.py::test_no_secrets_in_rows` |
| Report groups by intent | unit | `tests/test_telemetry_store.py::test_report_group_intent` |
| Report groups by stage | unit | `tests/test_telemetry_store.py::test_report_group_stage` |
| Report math (sums, counts) | unit | `tests/test_telemetry_store.py::test_report_math` |
| Absent store gives empty report | unit | `tests/test_telemetry_store.py::test_absent_store_empty_report` |
| Store stats returns counts | unit | `tests/test_telemetry_store.py::test_stats` |
| Dual-write fail-open (harness) | unit | `tests/test_telemetry_store.py::test_dual_write_fail_open` |
| GET /factory/telemetry returns report | integration | `tests/test_telemetry_store.py::test_serve_telemetry_endpoint` |

- New unit tests (`framework/ai-sdlc/tests/`):
  - `test_store.py` — schema, migrations, gitignore.
  - `test_telemetry_store.py` — sync, record, report, redaction, fail-open.
- Coverage impact: new modules, target >90% per #2046.

## Regression Strategy

- Existing tests affected: None — new modules and new functions in existing modules
  (cli.py new subparsers, serve.py new route, harness.py/runs.py new dual-write calls).
  The dual-write hooks wrap store calls in try/except so existing behavior is unchanged.
- Full suite command: `cd framework/ai-sdlc && python3 -m pytest tests/ -v`
- HTTP/Bruno API suite: N/A — no backend-api changes.
- Legacy paths at risk: None.

## Playwright Strategy

n/a — no UI surface. This change adds CLI commands and an HTTP endpoint; the dashboard
UI integration is a future wave.

## Deployment Strategy

- Flyway migration required: no — SQLite only, no PostgreSQL changes.
- Deployment order / coupling: None — the store is created lazily on first use.
- Configuration or `.env` keys to add: None.
- Feature flag: no.
- Smoke test after deploy (Gate 5): `aisdlc store stats --json` returns valid JSON;
  `aisdlc telemetry report --group stage --json` returns valid JSON.

## Rollback Strategy

- Revert safe: yes — reverting the PR removes the new modules and the dual-write hooks.
  The `.aisdlc/factory.db` file (if created) is git-ignored and harmless.
- Database rollback: none needed — no production database changes.
- Data written under the new behavior after revert: The store file persists but is
  orphaned. The evidence ledger (the authority) is unchanged.
- Blast radius if rollback is delayed: None — the store is supplementary.

## Open Questions

None — all design decisions are resolved by the #2393 contract.
