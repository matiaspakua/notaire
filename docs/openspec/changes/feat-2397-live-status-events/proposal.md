# Live Factory Status and Event Stream

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2397 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/2397_live_status_events` |
| Gate 1 status | passed |

## Objetivo

The factory has no way to observe its live state or receive real-time event updates.
This change adds `GET /factory/status` (current run, stage, step, previous and next
steps) and `GET /factory/events` (Server-Sent Events of ledger events with Last-Event-ID
replay) — making the factory's progress visible to the dashboard and any HTTP client.

## What Changes

1. **New module `aisdlc/status.py`** — pure function `status(ctx)` that derives the
   current factory state from the evidence ledger and the optional telemetry store.
   Returns a dict with intent, run, workflow, run_status, stage, step, previous steps
   (with outcome, duration, cost), next steps, and updated_at. Used by both CLI
   `aisdlc status --json` and `GET /factory/status`.
2. **New module `aisdlc/events_stream.py`** — SSE helpers: `ledger_rows()` reads the
   evidence ledger line-by-line with 1-based line-number IDs; `format_event()` frames
   a redacted row as an SSE frame; `stream()` polls the ledger and writes frames with
   keep-alive comments. Plain functions, no HTTP code.
3. **Serve routes in `aisdlc/serve.py`** — `GET /factory/status` returns the status
   payload as JSON (read scope). `GET /factory/events` streams SSE with proper headers
   (no Content-Length, X-Accel-Buffering: no, etc.), authentication, Last-Event-ID
   replay, and clean client-disconnect handling via threaded server.
4. **CLI extension in `aisdlc/cli.py`** — `aisdlc status [--json]` subcommand: JSON
   prints the full status payload; plain text prints a readable summary.
5. **OpenAPI contract update** — new endpoints `GET /factory/status` and
   `GET /factory/events` in `factory-control.openapi.yaml`.
6. **Tests** — status derivation (no runs, queued run, running run with step in flight,
   finished steps with duration and usd, next from workflow then line), SSE framing,
   Last-Event-ID replay, keep-alive, redaction, client disconnect.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Status is derived only from ledger and store — never invent a running state | CU76 / #2393 | New |
| A step is "in flight" only when step.started has no matching step.finished in a run whose derived status is running | CU76 / #2393 | New |
| SSE event IDs are 1-based ledger line numbers | CU76 / #2393 | New |
| SSE data is always redacted via `aisdlc.factory.redact` | CU76 / #2393 | New |
| SSE event kind is sanitized to `[A-Za-z0-9._-]` to prevent newline injection | CU76 / #2393 | New |
| `GET /factory/status` and `GET /factory/events` require read scope only | CU76 / #2393 | New |
| Invalid Last-Event-ID returns 400 problem+json | CU76 / #2393 | New |
| `aisdlc status` exits 0 for a valid repo regardless of run state | CU76 / #2393 | New |

## Capabilities

### New Capabilities

- `factory-live-status`: Live factory status endpoint and CLI command that derives
  current run, stage, step, previous and next steps from the evidence ledger and
  telemetry store, plus a Server-Sent Events stream of ledger events with replay
  support.

### Modified Capabilities

- `aisdlc-serve`: New GET endpoints `/factory/status` and `/factory/events` (read scope).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Not touched — this is the Python kit only |
| `frontend` | no | Not touched |
| `frontend-swing` | no | Not touched |
| `infra` / observability | no | Not touched |
| CI/CD (`.github/workflows`) | no | Not touched |
| `framework/ai-sdlc` | yes | New modules (status, events_stream), CLI extension, serve routes, tests |

### Surface area

- Entities: none — reads existing ledger and store; no new tables
- Endpoints: `GET /factory/status` (new, read scope, JSON), `GET /factory/events` (new, read scope, SSE stream)
- Database (Flyway `V{n}`): N/A — reads existing SQLite store read-only; no schema changes
- Configuration / `.env`: None
- Dependencies: None — stdlib only (`http.server`, `sqlite3`, `json`, `threading`, `time`, `contextlib`, `re`)

### Write set

- `framework/ai-sdlc/aisdlc/status.py`
- `framework/ai-sdlc/aisdlc/events_stream.py`
- `framework/ai-sdlc/aisdlc/cli.py`
- `framework/ai-sdlc/aisdlc/serve.py`
- `framework/ai-sdlc/aisdlc/contract.py`
- `framework/ai-sdlc/aisdlc/errors.py`
- `framework/ai-sdlc/tests/`
- `framework/ai-sdlc/openapi/factory-control.openapi.yaml`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/CHANGELOG.md`
- `docs/openspec/changes/feat-2397-live-status-events/`
- `docs/openspec/changes/archive/**feat-2397**/`
- `docs/openspec/specs/`

### Architecture review

No ADR required. The status module follows the existing kit pattern: stdlib-only Python,
pure functions with no module-level state, small functions (<30 lines). The SSE stream
uses the standard `http.server` threading model already employed by `aisdlc serve`. The
evidence ledger remains the authority; the store is read-only when present.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Add status CLI command and /factory/status, /factory/events endpoints |
| `framework/ai-sdlc/CHANGELOG.md` | Add `[Unreleased]` entry for live status and event stream |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | Add `GET /factory/status` and `GET /factory/events` endpoints |

## Out of Scope

- Dashboard UI integration (part G of #2393) — the dashboard will proxy the SSE stream;
  tracked in the dashboard issue.
- Intent model and CLI (`aisdlc intent submit`) — tracked in #2395.
- Production line view (`GET /factory/line`) — tracked in #2396.
- Contract.py `REQUIRED_PATHS` and `SCHEMA_MARKERS` updates — deferred to the docs step
  (Gate 3) per the issue instructions.
