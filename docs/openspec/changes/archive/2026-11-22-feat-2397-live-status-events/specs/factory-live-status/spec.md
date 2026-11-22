## ADDED Requirements

### Requirement: Status derivation with no runs

The system SHALL return an empty status shape when no runs exist in the ledger:
all fields null, `previous` and `next` as empty arrays, not an error.

#### Scenario: status with no runs returns empty shape

- **WHEN** the evidence ledger has no run.request events
- **THEN** `status(ctx)` returns `{"intent": null, "run": null, "workflow": null,
  "run_status": null, "stage": null, "step": null, "previous": [], "next": [],
  "updated_at": null}`

### Requirement: Status derivation for queued runs

The system SHALL NOT show a step as "in flight" for a run whose derived status
is "requested", "queued", or "blocked" — the run has not started executing.

#### Scenario: status with queued run never shows a running step

- **WHEN** the ledger has a run.request event but no step.started events, and the
  derived run status is "queued"
- **THEN** `status(ctx)` returns `step: null` and `stage: null` (no step is in flight)

### Requirement: Status derivation for running runs with step in flight

The system SHALL show a step as "in flight" only when a `step.started` event exists
with no matching `step.finished` in a run whose derived status is "running".

#### Scenario: status with running run shows step in flight

- **WHEN** the ledger has a run.request, a step.started for step "pre-push" with no
  step.finished, and the derived run status is "running"
- **THEN** `status(ctx)` returns `step: "pre-push"` and `stage` from the step's
  ledger event or profile stage mapping

### Requirement: Finished steps with duration and USD cost

The system SHALL include every completed step (with step.finished) in the `previous`
array with status (pass/fail/skip), started_at, finished_at, duration_s, and usd
(from the telemetry store when available).

#### Scenario: finished steps show duration and usd

- **WHEN** the ledger has a step.started (ts T1) and step.finished (ts T2, ok=true)
  for step "pre-push", and the telemetry store has a usd row for that run and step
- **THEN** `previous` contains an entry with `step: "pre-push"`, `status: "pass"`,
  `started_at: T1`, `finished_at: T2`, `duration_s: (T2-T1)`, and `usd` from the store

#### Scenario: finished step with skip status is not pass

- **WHEN** the ledger has a step.finished with ok=false and skip=true
- **THEN** the previous entry has `status: "skip"` (never "pass")

#### Scenario: finished step with no store usd shows null not zero

- **WHEN** the ledger has a completed step but the telemetry store does not exist
  or has no usd row for that step
- **THEN** the previous entry has `usd: null` (never 0 for unknown)

### Requirement: Next steps from workflow then line

The system SHALL populate `next` with workflow steps not yet started or finished,
followed by profile stages after the current/last completed stage, each tagged
with `source: "workflow"` or `source: "line"`.

#### Scenario: next steps from workflow then line

- **WHEN** the run's workflow defines steps ["pre-push", "pre-merge", "deploy"] and
  only "pre-push" has completed, and the profile has stages ["pre-push", "pre-merge",
  "deploy", "smoke-test"]
- **THEN** `next` contains `{"step": "pre-merge", "source": "workflow"}`,
  `{"step": "deploy", "source": "workflow"}`, `{"step": "smoke-test", "source": "line"}`

#### Scenario: next excludes steps already in previous or current

- **WHEN** step "pre-push" is in `previous` and step "pre-merge" is the current step
- **THEN** `next` does not contain "pre-push" or "pre-merge"

### Requirement: GET /factory/status authentication

The system SHALL require a valid Bearer token with read scope for `GET /factory/status`.
Requests without a token SHALL receive 401. Requests with a bad token SHALL receive
401. Requests with insufficient scope SHALL receive 403.

#### Scenario: GET /factory/status returns 401 without token

- **WHEN** a client sends `GET /factory/status` without an Authorization header
- **THEN** the response is 401 problem+json

#### Scenario: GET /factory/status returns 200 with valid read token

- **WHEN** a client sends `GET /factory/status` with a valid read-scope Bearer token
- **THEN** the response is 200 with the status JSON payload

### Requirement: SSE event framing

The system SHALL format each ledger row as an SSE frame: `id: <N>\nevent: <kind>\ndata:
<json>\n\n` where N is the 1-based line number, kind is sanitized to `[A-Za-z0-9._-]`,
and data is compact sort_keys JSON of the redacted row.

#### Scenario: SSE framing produces correct id, event, and data

- **WHEN** a ledger row on line 5 has `kind: "step.started"` and step "pre-push"
- **THEN** the SSE frame is `id: 5\nevent: step.started\ndata: {"kind":"step.started",...}\n\n`

#### Scenario: SSE event kind is sanitized

- **WHEN** a ledger row has a kind containing characters outside `[A-Za-z0-9._-]`
  (e.g., "step.started/foo")
- **THEN** the event kind replaces invalid characters with `_` (e.g., "step.started_foo")

#### Scenario: stage-run rows default to kind stage-run

- **WHEN** a ledger row has gates and stage keys but no `kind` field
- **THEN** the event kind is "stage-run"

### Requirement: SSE redaction of secrets

The system SHALL pass every ledger row through `aisdlc.factory.redact('', row)` before
emitting SSE data so no secret value is ever transmitted.

#### Scenario: secrets are redacted in SSE data

- **WHEN** a ledger row contains a field `api_key` with value "sk-123"
- **THEN** the SSE data JSON does not contain the key `api_key` or the value "sk-123"

### Requirement: Last-Event-ID replay

The system SHALL support the `Last-Event-ID` header to resume the SSE stream from
a specific point. Only rows with line number > the provided ID are emitted.

#### Scenario: Last-Event-ID replay returns only subsequent rows

- **WHEN** the ledger has 10 rows and the client sends `Last-Event-ID: 7`
- **THEN** the stream emits rows 8, 9, 10 and then continues polling for new rows

#### Scenario: ?after= query parameter replay

- **WHEN** the client sends `GET /factory/events?after=5` without Last-Event-ID
- **THEN** the stream emits rows 6 and later

#### Scenario: Last-Event-ID header takes precedence over ?after=

- **WHEN** the client sends both `Last-Event-ID: 8` and `?after=3`
- **THEN** the stream starts from row 9 (Last-Event-ID wins)

### Requirement: Invalid Last-Event-ID returns 400

The system SHALL reject invalid (non-integer or negative) Last-Event-ID values with
400 problem+json.

#### Scenario: invalid Last-Event-ID returns 400

- **WHEN** the client sends `Last-Event-ID: abc` or `Last-Event-ID: -1`
- **THEN** the response is 400 problem+json with an appropriate error code

### Requirement: Keep-alive comments

The system SHALL emit SSE comment frames (`: keep-alive\n\n`) when no new data has
been written for `keepalive_seconds` (default 15s) to prevent proxy timeouts.

#### Scenario: keep-alive comment emitted when idle

- **WHEN** the stream has been open for 15 seconds with no new ledger rows
- **THEN** a keep-alive comment `: keep-alive\n\n` is written

### Requirement: Live append pickup

The system SHALL detect new ledger rows appended while the stream is active and
emit them as SSE frames.

#### Scenario: live append to ledger is picked up by stream

- **WHEN** the stream is open with no new rows, and a new row is appended to the
  ledger file
- **THEN** the stream emits the new row within one polling interval (default 1s)

### Requirement: Client disconnect handling

The system SHALL handle client disconnection gracefully by catching write errors
and ending the stream loop cleanly.

#### Scenario: client disconnect ends the stream cleanly

- **WHEN** the client closes the connection while the stream is polling
- **THEN** the next write raises `BrokenPipeError`/`ConnectionResetError`/`OSError`,
  which is caught and the stream function returns without propagating the exception

### Requirement: CLI aisdlc status command

The system SHALL provide `aisdlc status [--json]` that prints the factory status.
`--json` outputs the full JSON payload (indent 2, sort_keys). Plain text outputs a
readable summary. Exit code is 0 for a valid repo.

#### Scenario: aisdlc status --json prints full payload

- **WHEN** `aisdlc status --json` is run in a valid factory directory
- **THEN** the output is valid JSON matching the status shape with exit code 0

#### Scenario: aisdlc status plain text prints summary

- **WHEN** `aisdlc status` is run without `--json`
- **THEN** the output shows run, intent, current stage/step, N previous, and next list
