## ADDED Requirements

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
