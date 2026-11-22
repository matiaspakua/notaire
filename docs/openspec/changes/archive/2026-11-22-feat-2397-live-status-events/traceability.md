# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2397 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2397-live-status-events/` | complete |
| Branch | `feat/2397_live_status_events` | created |
| Tasks | `tasks.md` | 9/12 complete (groups 10–12 pending PR/merge/deploy) |
| Commits | see below | recorded |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

### Commits

| SHA | Message |
|-----|---------|
| `ca14b0e3` | `docs(openspec): plan the live factory status and event stream` |
| `32cde2a1` | `test(aisdlc): failing tests for live factory status and event stream` |
| `f42e336c` | `test(aisdlc): cover in-flight step, next-from-line and plain-text status` |
| `05ddcdb5` | `feat(aisdlc): derive live factory status` |
| `8b6ab3f6` | `feat(aisdlc): stream ledger events over SSE` |
| `6b2fcdd8` | `docs(aisdlc): OpenAPI 1.4.0 for live status and event stream` |
| `7197a0f3` | `docs(aisdlc): make the factory OpenAPI valid YAML` |
| `3d888cb8` | `docs(aisdlc): README and changelog for live status and event stream` |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| status with no runs returns empty shape | `test_status.py::StatusEmptyTest::test_empty_shape_has_no_runs` | passing |
| status with queued run never shows a running step | `test_status.py::StatusWithRunTest::test_queued_run_never_invents_running` | passing |
| status with running run shows step in flight | `test_status.py::StatusWithRunTest::test_in_flight_step_shows_running` | passing |
| finished steps show duration and usd | `test_status.py::StatusWithRunTest::test_previous_steps_with_duration` | passing |
| finished step with skip status is not pass | `test_status.py::StatusWithRunTest::test_skip_is_never_pass` | passing |
| finished step with no store usd shows null not zero | `test_status.py::StatusWithRunTest::test_previous_step_usd_none_without_telemetry` | passing |
| next steps from workflow then line | `test_status.py::StatusWithRunTest::test_next_from_workflow_and_line` | passing |
| next excludes steps already in previous or current | `test_status.py::StatusWithRunTest::test_next_from_workflow_and_line` | passing |
| GET /factory/status returns 401 without token | `test_serve_status_events.py::ServeStatusEventsTest::test_status_401_without_token` | passing |
| GET /factory/status returns 200 with valid read token | `test_serve_status_events.py::ServeStatusEventsTest::test_status_200_with_token_empty_shape` | passing |
| SSE framing produces correct id, event, and data | `test_events_stream.py::FormatEventTest::test_format_event_produces_exact_bytes` | passing |
| SSE event kind is sanitized | `test_events_stream.py::FormatEventTest::test_format_event_sanitizes_kind` | passing |
| stage-run rows default to kind stage-run | `test_events_stream.py::FormatEventTest::test_format_event_stage_run_kind` | passing |
| secrets are redacted in SSE data | `test_events_stream.py::FormatEventTest::test_format_event_redacts_secrets` | passing |
| Last-Event-ID replay returns only subsequent rows | `test_events_stream.py::ParseLastEventIdTest::test_parse_last_event_id_from_header`, `test_serve_status_events.py::ServeStatusEventsTest::test_events_replay_with_last_event_id` | passing |
| ?after= query parameter replay | `test_events_stream.py::ParseLastEventIdTest::test_parse_last_event_id_from_query`, `test_serve_status_events.py::ServeStatusEventsTest::test_events_replay_with_query_after` | passing |
| Last-Event-ID header takes precedence over ?after= | `test_events_stream.py::ParseLastEventIdTest::test_parse_last_event_id_header_wins_over_query` | passing |
| invalid Last-Event-ID returns 400 | `test_events_stream.py::ParseLastEventIdTest::test_parse_last_event_id_invalid_raises`, `test_serve_status_events.py::ServeStatusEventsTest::test_events_invalid_last_event_id_returns_400` | passing |
| keep-alive comment emitted when idle | `test_events_stream.py::StreamTest::test_stream_keepalive_when_no_rows` | passing |
| live append to ledger is picked up by stream | `test_events_stream.py::StreamTest::test_stream_emits_new_row_after_fake_sleep`, `test_serve_status_events.py::ServeStatusEventsTest::test_events_live_append_arrives` | passing |
| client disconnect ends the stream cleanly | `test_events_stream.py::StreamTest::test_stream_broken_pipe_ends_cleanly` | passing |
| aisdlc status --json prints full payload | `test_serve_status_events.py::CliStatusTest::test_status_json_no_run` | passing |
| aisdlc status plain text prints summary | `test_serve_status_events.py::CliStatusTest::test_status_plain_text_with_in_flight_step` | passing |
| status with in-flight step via HTTP | `test_serve_status_events.py::ServeStatusInFlightTest::test_status_http_in_flight_step` | passing |
| intent propagation from run request | `test_status.py::StatusWithRunTest::test_intent_propagation_from_run_request` | passing |
| usd summed across multiple telemetry rows | `test_status.py::StatusWithRunTest::test_usd_summed_across_multiple_telemetry_rows` | passing |
| usd not mixed with another run | `test_status.py::StatusWithRunTest::test_usd_not_mixed_with_another_run` | passing |
| run selection prefers not finished | `test_status.py::StatusWithRunTest::test_run_selection_prefers_not_finished` | passing |
| finished run step and stage are none | `test_status.py::StatusWithRunTest::test_finished_run_step_and_stage_are_none` | passing |
| blocked run step and stage are none | `test_status.py::StatusWithRunTest::test_blocked_run_step_and_stage_are_none` | passing |
| stage from step ledger event | `test_status.py::StatusWithRunTest::test_stage_from_step_ledger_event` | passing |
| status after run start shows queued | `test_status.py::StatusWithRunTest::test_status_after_run_start_shows_queued` | passing |
| previous step usd from telemetry store | `test_status.py::StatusWithRunTest::test_previous_step_usd_from_telemetry_store` | passing |
| ledger rows returns all rows | `test_events_stream.py::LedgerRowsTest::test_ledger_rows_returns_all_rows` | passing |
| ledger rows after filter | `test_events_stream.py::LedgerRowsTest::test_ledger_rows_after_filter` | passing |
| ledger rows skips partial trailing line | `test_events_stream.py::LedgerRowsTest::test_ledger_rows_skips_partial_trailing_line` | passing |
| ledger rows picks up completed trailing line | `test_events_stream.py::LedgerRowsTest::test_ledger_rows_picks_up_completed_trailing_line` | passing |
| ledger rows skips blank lines | `test_events_stream.py::LedgerRowsTest::test_ledger_rows_skips_blank_lines` | passing |
| SSE default kind is ledger | `test_events_stream.py::FormatEventTest::test_format_event_ledger_default_kind` | passing |
| stream replays existing rows | `test_events_stream.py::StreamTest::test_stream_replays_existing_rows` | passing |
| events 401 without token | `test_serve_status_events.py::ServeStatusEventsTest::test_events_401_without_token` | passing |
| events 200 with token | `test_serve_status_events.py::ServeStatusEventsTest::test_events_200_with_token` | passing |
| SSE and status not blocked (threading) | `test_serve_status_events.py::ServeStatusEventsTest::test_sse_and_status_not_blocked` | passing |
| events close and reconnect | `test_serve_status_events.py::ServeStatusEventsTest::test_events_close_and_reconnect` | passing |
| status 200 with run | `test_serve_status_events.py::ServeStatusEventsTest::test_status_200_with_run` | passing |
| parse_last_event_id defaults to zero | `test_events_stream.py::ParseLastEventIdTest::test_parse_last_event_id_defaults_to_zero` | passing |
| parse_last_event_id negative raises | `test_events_stream.py::ParseLastEventIdTest::test_parse_last_event_id_negative_raises` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | updated | `3d888cb8` |
| `framework/ai-sdlc/CHANGELOG.md` | updated | `3d888cb8` |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | updated | `6b2fcdd8`, `7197a0f3` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `ca14b0e3` — OpenSpec change created and validated |
| 2 | Failing tests written, test cases designed | yes | `32cde2a1`, `f42e336c` — failing tests committed before implementation |
| 3 | Suite green, docs updated | yes | Suite 585 passed, 4 skipped; README, CHANGELOG, OpenAPI updated |
| 4 | CI green, review approved, no conflicts | pending | Awaiting PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Awaiting merge |

## Exceptions

- No new error codes were needed; `invalid_body` from `errors.py` is reused for invalid Last-Event-ID values.
- Two test-bug fixes in `test_events_stream.py`: ledger directory creation helper (`mkdir(parents=True)` missing) and broken-pipe test `keepalive_seconds=0.0` so a write actually occurs.
