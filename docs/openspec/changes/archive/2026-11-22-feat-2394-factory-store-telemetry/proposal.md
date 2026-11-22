# Factory Store — Persistent SQLite Telemetry Store

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2394 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/2394_factory_store_telemetry` |
| Gate 1 status | passed |

## Objetivo

The factory has no persistent store: telemetry, events, and cost data live only in
ephemeral JSONL files and in-memory OTel spans. This change introduces a SQLite store
(`.aisdlc/factory.db`) with schema migrations, an idempotent sync from evidence/RepoLens,
a telemetry CLI and API endpoint, and dual-write hooks in harness and run requestors —
making the factory observable across sessions.

## What Changes

1. **New module `aisdlc/store.py`** — SQLite database at `.aisdlc/factory.db` with tables
   `events`, `intents`, `telemetry`, `benchmarks`, `config_snapshots` and a `schema_migrations`
   version table. Idempotent migration function. Stdlib only (`sqlite3`).
2. **New module `aisdlc/telemetry_store.py`** — `sync()` imports evidence ledger, cost log,
   and RepoLens `.repolens/usage.jsonl` idempotently (idempotency key = source + line hash).
   `stats()` returns row counts. `record()` inserts a telemetry row with redaction.
   `report()` groups by intent, stage, agent, skill, or tool with time, cost, and failure counts.
3. **CLI extensions in `aisdlc/cli.py`** — new subparsers: `aisdlc store sync`,
   `aisdlc store stats --json`, `aisdlc telemetry record`, `aisdlc telemetry report --group --json`.
4. **Dual-write mirror** — `aisdlc/evidence.py::_append()` (single choke point for ledger
   and cost writes) mirrors each line to the store via `telemetry_store.mirror_line`
   (fail-open: store failure never breaks the primary path).
5. **Serve route in `aisdlc/serve.py`** — `GET /factory/telemetry?group=intent|stage|agent|skill|tool`
   returns the telemetry report (read scope).
6. **OpenAPI contract update** — new endpoint `GET /factory/telemetry` in
   `factory-control.openapi.yaml`.
7. **Git-ignore for `.aisdlc/factory.db`** — generalizes the `.aisdlc/.gitignore` pattern
   used by `serve_token._keep_out_of_git`.
8. **Tests** — sync idempotency, report math, no secrets in rows, absent store yields empty report.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Store file must be git-ignored (`.aisdlc/factory.db`) | CU76 / #2393 | New |
| Never store prompt text, tokens, or secrets in any telemetry row | CU76 / #2393 | New |
| RepoLens `input` query text and `files` must never be stored | CU76 / #2393 | New |
| Sync is idempotent — re-running produces no duplicate rows | CU76 / #2393 | New |
| Store failure must never break harness or run writers (fail-open) | CU76 / #2393 | New |
| Schema migrations are idempotent and versioned | CU76 / #2393 | New |
| `GET /factory/telemetry` requires read scope only | CU76 / #2393 | New |

## Capabilities

### New Capabilities

- `factory-store-telemetry`: Persistent SQLite store for factory telemetry, events,
  benchmarks, and configuration snapshots with CLI record/report, idempotent sync from
  evidence ledger and RepoLens, and a read-only HTTP endpoint.

### Modified Capabilities

- `ai-sdlc-framework`: Harness report and run request now dual-write to the store
  (new behavior: events table population alongside the existing evidence ledger).
- `aisdlc-serve`: New GET endpoint `/factory/telemetry` (read scope).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Not touched — this is the Python kit only |
| `frontend` | no | Not touched |
| `frontend-swing` | no | Not touched |
| `infra` / observability | no | Not touched |
| CI/CD (`.github/workflows`) | no | Not touched |
| `framework/ai-sdlc` | yes | New modules (store, telemetry_store), CLI extensions, serve route, dual-write hooks, tests |

### Surface area

- Entities: SQLite tables `events`, `intents`, `telemetry`, `benchmarks`, `config_snapshots`, `schema_migrations`
- Endpoints: `GET /factory/telemetry?group=intent|stage|agent|skill|tool` (new, read scope)
- Database (Flyway `V{n}`): N/A — SQLite only, no Flyway migration
- Configuration / `.env`: None — store path is fixed at `.aisdlc/factory.db`
- Dependencies: None — stdlib only (`sqlite3`, `hashlib`, `json`)

### Write set

- `framework/ai-sdlc/aisdlc/store.py`
- `framework/ai-sdlc/aisdlc/telemetry_store.py`
- `framework/ai-sdlc/aisdlc/cli.py`
- `framework/ai-sdlc/aisdlc/serve.py`
- `framework/ai-sdlc/aisdlc/evidence.py`
- `framework/ai-sdlc/aisdlc/contract.py`
- `framework/ai-sdlc/aisdlc/serve_token.py`
- `framework/ai-sdlc/aisdlc/errors.py`
- `framework/ai-sdlc/tests/`
- `framework/ai-sdlc/openapi/factory-control.openapi.yaml`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/CHANGELOG.md`
- `docs/openspec/changes/feat-2394-factory-store-telemetry/`
- `docs/openspec/changes/archive/**feat-2394**/`
- `docs/openspec/specs/`

### Architecture review

No ADR required. The store follows the existing kit pattern: stdlib-only Python,
`.aisdlc/` directory for local state (same as serve tokens and evidence), and
fail-open semantics consistent with the OTel telemetry port. The SQLite database
is a mirror of the evidence ledger (which remains the hash-chained record of
authority), not a replacement.

## Documentation Impact

<!-- Which PERMANENT documentation must be updated before merge (Gate 3).
     Permanent docs are the single source of truth; this proposal is not.
     Do not duplicate their content here - name the file and what changes in it.
     Mark documents to be archived into docs/000-archive/. -->

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Add store and telemetry CLI commands to the command reference |
| `framework/ai-sdlc/CHANGELOG.md` | Add `[Unreleased]` entry for factory store and telemetry |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | Add `GET /factory/telemetry` endpoint |

## Out of Scope

- Intent model and CLI (`aisdlc intent submit`) — tracked in #2395.
- Production line view (`GET /factory/line`) — tracked in #2396.
- Live state and events SSE (`GET /factory/status`, `GET /factory/events`) — future wave.
- Dashboard UI integration — future wave (G in #2393).
