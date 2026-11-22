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
| Tasks | `tasks.md` | 0/8 complete (groups 9–12 pending PR/merge/deploy) |
| Commits | see below | recorded |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

### Commits

| SHA | Message |
|-----|---------|
| <!-- SHA --> | `docs(openspec): plan the live factory status and event stream` |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| status with no runs returns empty shape | `test_status.py::test_status_no_runs` | pending |
| status with queued run never shows a running step | `test_status.py::test_status_queued_no_running_step` | pending |
| status with running run shows step in flight | `test_status.py::test_status_running_step_in_flight` | pending |
| finished steps show duration and usd | `test_status.py::test_status_finished_steps_duration_usd` | pending |
| next steps from workflow then line | `test_status.py::test_status_next_workflow_then_line` | pending |
| GET /factory/status returns 401 without token | `test_events_stream.py::test_status_401_no_token` | pending |
| SSE framing produces correct id/event/data | `test_events_stream.py::test_format_event` | pending |
| Last-Event-ID replay returns only subsequent rows | `test_events_stream.py::test_last_event_id_replay` | pending |
| ?after= replay returns only subsequent rows | `test_events_stream.py::test_after_query_replay` | pending |
| live append to ledger is picked up by stream | `test_events_stream.py::test_live_append_picked_up` | pending |
| keep-alive comment emitted when idle | `test_events_stream.py::test_keepalive_comment` | pending |
| secrets are redacted in SSE data | `test_events_stream.py::test_redaction_of_secrets` | pending |
| invalid Last-Event-ID returns 400 | `test_events_stream.py::test_invalid_last_event_id_400` | pending |
| client disconnect ends the stream cleanly | `test_events_stream.py::test_client_disconnect` | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | pending | <!-- SHA --> |
| `framework/ai-sdlc/CHANGELOG.md` | pending | <!-- SHA --> |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | pending | <!-- SHA --> |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | OpenSpec change created and validated |
| 2 | Failing tests written, test cases designed | pending | Awaiting implementation |
| 3 | Suite green, coverage held, docs updated | pending | Awaiting implementation |
| 4 | CI green, review approved, no conflicts | pending | Awaiting PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Awaiting merge |

## Exceptions

None at this time.
