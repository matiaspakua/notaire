# factory-dashboard-production-line Specification

## Purpose

TBD - created by archiving change feat-2400-dashboard-production-line. Update Purpose after archive.

## Requirements

### Requirement: Server proxies DELETE with CSRF and If-Match

The dashboard server SHALL proxy `DELETE /factory/catalogs/{kind}/{id}` to the kit with the
user's bearer token, `X-AISDLC-CSRF` (equal to the token), and `If-Match` forwarded from the
browser. It SHALL validate the dashboard's `X-Dashboard-CSRF` before forwarding.

#### Scenario: DELETE proxied with CSRF and If-Match

- **WHEN** a signed-in user sends `DELETE /api/workspaces/:ws/factory/catalogs/agents/my-agent`
  with `X-Dashboard-CSRF` and `If-Match: "3"`
- **THEN** the server forwards `DELETE /factory/catalogs/agents/my-agent` to the kit with
  `Authorization: Bearer <token>`, `X-AISDLC-CSRF: <token>`, and `If-Match: "3"`, and returns
  the kit's response

#### Scenario: DELETE refused without CSRF

- **WHEN** a request omits `X-Dashboard-CSRF`
- **THEN** the server returns `403 csrf_required` and forwards nothing to the kit

### Requirement: Server proxies SSE events with Last-Event-ID replay

The dashboard server SHALL proxy `GET /factory/events` as an SSE stream: it opens an `EventSource`
to the kit with the bearer token, forwards the browser's `Last-Event-ID` header, relays frames
verbatim to the client, and closes the upstream stream when the client disconnects.

#### Scenario: SSE proxy forwards Last-Event-ID

- **WHEN** the browser opens `GET /api/workspaces/:ws/factory/events` with `Last-Event-ID: 5`
- **THEN** the server opens `EventSource` to the kit with `Last-Event-ID: 5` and relays frames
  to the browser

#### Scenario: SSE proxy cleans up on client disconnect

- **WHEN** the browser closes the SSE connection
- **THEN** the server closes the upstream `EventSource` to the kit

### Requirement: Production line timeline shows stations and gates

The dashboard SHALL render a horizontal timeline of stations from `GET /factory/line`, with gates
between stations displayed as pills labeled "required" or "advisory". Expanding a station reveals
its agents, skills, tools, inputs, outputs, and guardrails.

#### Scenario: Production line timeline renders stations and gates

- **WHEN** `/factory/line` returns 5 stages with gates
- **THEN** the timeline shows 5 stations with gates between them, each gate pill labeled required
  or advisory

#### Scenario: Station expand shows agents, skills, tools

- **WHEN** a stage has agents, skills, mcp, and tools
- **THEN** expanding the station lists them under their labels

### Requirement: Current run position from /factory/status

The dashboard SHALL highlight the station corresponding to the current stage from
`GET /factory/status`, show previous steps with their outcome (pass/fail/skip), duration, and cost,
and show next steps.

#### Scenario: Current run position highlighted

- **WHEN** `/factory/status` returns `stage: "prove"` and `step: "test"`
- **THEN** the "prove" station is highlighted and the step "test" is shown as current

#### Scenario: Previous and next steps shown

- **WHEN** `/factory/status` returns 2 previous steps and 3 next steps
- **THEN** the timeline shows the 2 previous steps with status pills and the 3 next steps

### Requirement: Intent bar always visible with drawer

The dashboard SHALL show an always-visible bar at the bottom with the current intent's title and
status. Clicking the bar opens a drawer with the full intent text, acceptance criteria, references
as links, and source. An intake form in the drawer submits to `POST /factory/intents`.

#### Scenario: Intent bar always visible

- **WHEN** `/factory/status` returns a non-null intent id
- **THEN** the intent bar shows the intent's title and status on every view

#### Scenario: Intent drawer shows full intent

- **WHEN** the user clicks the intent bar
- **THEN** a drawer opens with the intent's text, acceptance criteria list, references as clickable
  links, and the source system

#### Scenario: Intake form posts to /factory/intents

- **WHEN** the user fills the intake form with text and submits
- **THEN** the dashboard sends `POST /factory/intents` with the body and shows the new intent in
  the bar

### Requirement: Live sync via SSE with polling fallback

The dashboard SHALL connect to the SSE proxy URL on mount, refresh status, line, and intent data
on each event, and show a live indicator dot in the sidebar. When SSE is unavailable, it SHALL
fall back to polling `/factory/status` every 5 seconds.

#### Scenario: Live sync via SSE updates status

- **WHEN** the SSE stream emits a `step.finished` event
- **THEN** the dashboard refreshes `/factory/status` and updates the timeline

#### Scenario: Polling fallback when SSE unavailable

- **WHEN** the SSE connection fails (kit not supporting SSE)
- **THEN** the dashboard polls `/factory/status` every 5 seconds and the live indicator shows
  a polling state

### Requirement: Config-as-code file tree and read-only viewer

The dashboard SHALL render a file tree from `GET /factory/config/files` with kind badges
(profile, policy, context, catalog, project, skill). Clicking a file opens a read-only viewer
with line numbers from `GET /factory/config/files/{path}`.

#### Scenario: Config file tree renders

- **WHEN** `/factory/config/files` returns 10 files
- **THEN** the Projects view shows a tree with 10 entries, each with a kind badge

#### Scenario: Config file viewer shows content with line numbers

- **WHEN** the user clicks a config file
- **THEN** a viewer opens with the file content and line numbers; the content is read-only

### Requirement: Delete catalog entry with inline confirmation

The dashboard SHALL offer a delete button on catalog entries that opens an inline confirmation
dialog (not `window.confirm`) showing the entry name and version. On confirm, it sends
`DELETE /factory/catalogs/{kind}/{id}` with `If-Match`. On 409, it shows a version conflict
message.

#### Scenario: Delete with inline confirmation sends If-Match

- **WHEN** the user confirms delete on an entry with version 3
- **THEN** the dashboard sends `DELETE` with `If-Match: "3"` and removes the entry on 204

#### Scenario: Delete shows version conflict on 409

- **WHEN** the kit returns 409 on delete
- **THEN** the dashboard shows "version conflict" and keeps the entry

### Requirement: Telemetry panel shows time, cost, failures, RepoLens

The dashboard SHALL render a telemetry panel in the Metrics view with tabs for group dimensions
(intent, stage, agent, skill, tool). Each tab shows rows with key, runs, duration, cost, and
failures. The panel shows total duration, total cost, total failures, failures by integration,
and RepoLens calls with avg latency and per-tool breakdown.

#### Scenario: Telemetry panel shows time, cost, failures

- **WHEN** `/factory/telemetry?group=stage` returns rows
- **THEN** the panel shows a table with stage names, runs, duration_s, usd, and failures

#### Scenario: RepoLens metrics shown

- **WHEN** `/factory/telemetry` returns a repolens object
- **THEN** the panel shows total calls, avg_ms, and a per-tool call count breakdown

### Requirement: Accessibility — WCAG AA, no colour-only status

The dashboard SHALL meet WCAG AA contrast ratios, SHALL label every status indicator with text
(never colour alone), and SHALL be keyboard-navigable.

#### Scenario: WCAG AA no colour-only status

- **WHEN** axe core runs on the production line, intent bar, and metrics views
- **THEN** no violations are found for color contrast or colour-only information

### Requirement: Overview shows the line as a diagram

The Overview SHALL draw the production line as a rail with one station per stage from
`GET /factory/line`, in line order. Each station SHALL show its stage name, its state in words
(Done, Running, Failed, Next or Idle), its kind and its throughput. The diagram SHALL be operable
with the keyboard, and SHALL NOT make the page scroll sideways at 320px.

#### Scenario: One station per stage in line order

- **WHEN** the kit's line has the stages plan, red, pre-push, pre-pr and merge
- **THEN** the Overview shows five stations in that order on one rail

#### Scenario: The running station holds the token

- **WHEN** `GET /factory/status` reports a running run whose current stage is red
- **THEN** plan is Done, red is Running and holds the token, and the later stations are Next

#### Scenario: A failed run marks its station

- **WHEN** the run's status is failed at stage pre-push
- **THEN** the pre-push station says Failed, and the stations before it say Done

#### Scenario: Small screens get a vertical line

- **WHEN** the viewport is 320px wide
- **THEN** the stations stack on a vertical rail and the page has no horizontal scroll

### Requirement: Stations show their kind

Each station SHALL show a distinct marker and word for its kind: AI agent when the stage lists
agents, Human decision for the merge stage, and Deterministic step otherwise.

#### Scenario: Kinds follow the line data

- **WHEN** plan lists agents, merge lists none and release lists none
- **THEN** plan says AI agent, merge says Human decision, and release says Deterministic step

### Requirement: Gates show their last evidence state

Each station SHALL list its gates with the last state that `GET /factory/report` records for that
stage and gate: passed, failed, not evaluable (skip or todo) or not run (no evidence). A skipped
gate SHALL NEVER be shown as passed. The detail panel SHALL show each gate's mode, pass rate,
median time and run count.

#### Scenario: A skipped gate is not evaluable

- **WHEN** the report's last status for a gate is skip
- **THEN** the gate shows "Not evaluable", not "Passed"

#### Scenario: A gate without evidence says so

- **WHEN** the report has no entry for a gate
- **THEN** the gate shows "Not run"

### Requirement: The correction loop is drawn

When the line has both the red and pre-push stages, the diagram SHALL draw a loop from pre-push
back to red, labelled with the rule "One correction per blocker; if it persists, a human
decision".

#### Scenario: Loop between pre-push and red

- **WHEN** the line contains red and pre-push
- **THEN** a correction loop and its rule appear between those stations

### Requirement: Stations show throughput and cost

Each station SHALL show the runs through it, their average time, cost and failures, summed from
`GET /factory/telemetry?group=stage` rows whose key is the stage or one of the workflow's steps
mapped to that stage. With no rows the station SHALL say "no runs yet"; unknown cost SHALL be
named, never shown as zero.

#### Scenario: Step rows sum into their station

- **WHEN** the telemetry rows spec (2 runs, 60 s) and plan (1 run, 30 s) exist and the workflow
  maps spec to plan
- **THEN** the plan station shows 3 runs and a 30 s average

#### Scenario: No telemetry

- **WHEN** no telemetry row maps to the release station
- **THEN** the release station says "no runs yet"

### Requirement: Intent progress strip

Above the line, the Overview SHALL show the run's intent title, or that no intent is attached;
stations completed of total as a segmented bar with an accessible name; the elapsed time; the
cost so far, or that cost was not reported; and the current step.

#### Scenario: Progress of a running intent

- **WHEN** a run is at the third of seven stations with two finished steps costing $0.20 and $0.42
- **THEN** the strip reads "2 of 7 stations complete" and shows $0.62

#### Scenario: No intent attached

- **WHEN** the status has no intent
- **THEN** the strip says that no intent is attached to the run and offers to submit one

### Requirement: Station detail panel

Selecting a station with a click, Enter or Space SHALL show its kind, agents, skills, MCP and
tools, model, effort, inputs, outputs and grants, and a table of its gates, in a polite live
region. The running station SHALL be selected by default; with no run, the first station is.

#### Scenario: Keyboard selection

- **WHEN** a person moves to the pre-pr station with the arrow keys and presses Enter
- **THEN** the detail panel shows pre-pr's agents, skills and gates table
