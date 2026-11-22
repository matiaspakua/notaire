> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The kit records evidence in an append-only JSONL ledger and optionally mirrors it
to a SQLite store (#2394). The `aisdlc serve` HTTP server already handles
authentication, scoped tokens, and JSON dispatch. Issue #2393 (Live state D)
defines the shared contract for `GET /factory/status` and `GET /factory/events`.
This design implements Live state D building on #2394's store and the existing
serve infrastructure.

## Goals / Non-Goals

**Goals:**
- `GET /factory/status` returns derived factory state: current intent, run, stage,
  step, previous steps (outcome, duration, cost), next steps, updated_at.
- `GET /factory/events` streams SSE of ledger events with Last-Event-ID replay,
  keep-alive comments, and clean client-disconnect handling.
- CLI `aisdlc status [--json]` prints the same status payload.
- Status derived only from ledger and store — never invent a running state.
- SSE data always redacted; event kind sanitized to prevent newline injection.
- Stdlib only, small functions (<30 lines), no module-level state.

**Non-Goals:**
- Dashboard UI integration (part G of #2393) — the dashboard proxies the stream.
- Intent model and CLI (tracked in #2395).
- Production line view (tracked in #2396).
- Creating the telemetry store — only read when it exists.

## Decisions

### Line-number event IDs for SSE

**Why line numbers?** Ledger rows have no stable id. The 1-based line number is
monotonic, unique within a file, and naturally supports "send me everything after
line N" via Last-Event-ID or `?after=`. The alternative (hashing each row) would
require full-file scans to map a hash back to a position.

### Polling the ledger file at 1 second

**Why poll instead of inotify?** The ledger is an append-only JSONL file written
by the evidence module. Polling at 1s is simple, stdlib-only, and matches the
expected update frequency (steps complete in seconds to minutes). `inotify` would
add platform-specific code and complexity for marginal gain.

### 15-second keep-alive

**Why 15s?** Common proxy timeouts (nginx, HAProxy) are 60s. A 15s keep-alive
comment (`: keep-alive\n\n`) ensures the connection stays alive through intermediate
proxies without excessive traffic. The SSE spec allows comment-only frames.

### Threaded HTTP server for SSE

**Why ThreadingHTTPServer?** Each SSE client holds a long-lived connection. A
single-threaded server would block all other requests. `ThreadingHTTPServer` with
`daemon_threads = True` ensures each stream runs independently and server shutdown
is not blocked by live clients. A `threading.Event` signals streams to stop on
shutdown.

### Redaction via `aisdlc.factory.redact`

**Why redact at emit time?** The ledger may contain secrets (API keys, tokens) in
arbitrary data fields. Passing every row through `aisdlc.factory.redact('', row)`
before emitting ensures no secret value is ever sent over the SSE stream. This is
the same redaction function used by the telemetry store (#2394).

### Status derivation rules

**Never invent a running state.** A step is "in flight" only when:
1. A `step.started` event exists in the ledger for that step.
2. No matching `step.finished` event exists.
3. The run's derived status (from `runs.derive_status`) is "running".

If the run's derived status is "requested", "queued", or "blocked", no step is
considered in flight — the run hasn't started executing yet.

**Current run selection:** The latest run (by last ledger event timestamp, falling
back to `requested_at`) whose derived status is not "finished". If all runs are
finished, the latest run overall. If no runs exist, return the empty shape
(all nulls, previous=[], next=[]).

**Stage of a step:** The step's own `stage` key in its ledger events when present,
else if the step id equals a profile stage id (`ctx.profile.stages()`), use it,
else null.

**Next steps:** From the run's workflow (steps not yet started or finished, in
order), then from the profile's stage line after the current stage (or last
completed stage, or all stages when nothing ran), excluding steps already named
in previous/current/next-from-workflow. Each next item carries `source:
"workflow"|"line"`.

## Riesgos / Trade-offs

- [Stale status if ledger write is slow] → Mitigation: status reads the ledger
  directly; the ledger is the authority. The store is supplementary and read-only.
- [SSE connection leaks on abnormal disconnect] → Mitigation: `stream()` catches
  `BrokenPipeError`, `ConnectionResetError`, and `OSError` and returns cleanly.
  `self.close_connection = True` ensures the HTTP handler releases resources.
- [Large ledger files slow down polling] → Mitigation: `ledger_rows()` reads only
  lines after the given line number, not the entire file. For very large ledgers,
  this is still O(N) in line count but fast for typical factory runs (<1000 lines).
- [Redaction may drop useful debug data] → Acceptable trade-off: security over
  debuggability. The raw ledger file remains available for authorized inspection.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| status with no runs returns empty shape | unit | `tests/test_status.py::test_status_no_runs` |
| status with queued run never shows a running step | unit | `tests/test_status.py::test_status_queued_no_running_step` |
| status with running run shows step in flight | unit | `tests/test_status.py::test_status_running_step_in_flight` |
| finished steps show duration and usd | unit | `tests/test_status.py::test_status_finished_steps_duration_usd` |
| next steps from workflow then line | unit | `tests/test_status.py::test_status_next_workflow_then_line` |
| GET /factory/status returns 401 without token | integration | `tests/test_events_stream.py::test_status_401_no_token` |
| SSE framing produces correct id/event/data | unit | `tests/test_events_stream.py::test_format_event` |
| Last-Event-ID replay returns only subsequent rows | unit | `tests/test_events_stream.py::test_last_event_id_replay` |
| ?after= replay returns only subsequent rows | unit | `tests/test_events_stream.py::test_after_query_replay` |
| live append to ledger is picked up by stream | unit | `tests/test_events_stream.py::test_live_append_picked_up` |
| keep-alive comment emitted when idle | unit | `tests/test_events_stream.py::test_keepalive_comment` |
| secrets are redacted in SSE data | unit | `tests/test_events_stream.py::test_redaction_of_secrets` |
| invalid Last-Event-ID returns 400 | unit | `tests/test_events_stream.py::test_invalid_last_event_id_400` |
| client disconnect ends the stream cleanly | unit | `tests/test_events_stream.py::test_client_disconnect` |

- New unit tests (`framework/ai-sdlc/tests/`):
  - `test_status.py` — status derivation: no runs, queued, running with step in
    flight, finished steps with duration/usd, next from workflow then line.
  - `test_events_stream.py` — SSE framing, Last-Event-ID replay, ?after= replay,
    live append, keep-alive, redaction, invalid Last-Event-ID, client disconnect.
- Coverage impact: new modules, target >90% per #2046.

## Regression Strategy

- Existing tests affected: None — new modules (status.py, events_stream.py) and
  new functions in existing modules (cli.py new subparser, serve.py new routes).
  The new serve routes are added before the JSON dispatch in `_handle`, so existing
  routes are unaffected.
- Full suite command: `cd framework/ai-sdlc && python3 -m pytest tests/ -v`
- HTTP/Bruno API suite: N/A — no backend-api changes.
- Legacy paths at risk: None.

## Playwright Strategy

n/a — no UI surface. This change adds CLI commands and HTTP endpoints; the dashboard
UI integration is part G of #2393, tracked separately.

## Deployment Strategy

- Flyway migration required: no — no PostgreSQL changes; reads existing SQLite store read-only.
- Deployment order / coupling: None — new endpoints are additive to the serve contract.
- Configuration or `.env` keys to add: None.
- Feature flag: no.
- Smoke test after deploy (Gate 5): `aisdlc status --json` returns valid JSON;
  `curl -H "Authorization: Bearer <token>" http://localhost:<port>/factory/status`
  returns 200 with valid JSON.

## Rollback Strategy

- Revert safe: yes — reverting the PR removes the new modules and the new serve
  routes. No data is written by this change.
- Database rollback: none needed — no database changes.
- Data written under the new behavior after revert: None — this change is read-only.
- Blast radius if rollback is delayed: None — new endpoints are additive; existing
  endpoints are unaffected.

## Open Questions

None — all design decisions are resolved by the #2393 contract and the issue instructions.
