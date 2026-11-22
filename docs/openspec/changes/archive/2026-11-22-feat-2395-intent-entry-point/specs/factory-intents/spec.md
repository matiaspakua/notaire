## Purpose

The AI SDLC factory needs a single Intent entity and entry point so that CLI operators, HTTP clients, MCP tools and the future dashboard all create, list and inspect intents through one service layer.

## ADDED Requirements

### Requirement: Intent model and versioned persistence

The kit SHALL persist Intent entities with fields `id`, `kind` (`prompt|reference|spec`),
`title`, `text`, `source` (`{system: github|jira|url|file|"", ref}`), `acceptance[]`,
`references[{label,url,kind}]` (with reference `kind` in `architecture|security|adr|standard|other`),
`status`, `created_by`, `created_at`, and `run_ids[]`. For prompt intents, `source` SHALL be
`{"system": "", "ref": ""}`. Intents SHALL be stored under
`.aisdlc/intents/<id>/current.json` with immutable version records under
`.aisdlc/intents/<id>/versions/{version}.json`. On submission, the kit SHALL append an
evidence ledger event recording `id`, `kind`, `source`, and `actor` without logging
intent `text`. If `aisdlc.store` is available, the kit SHALL mirror the intent;
if unavailable, store mirroring SHALL be a silent no-op.

#### Scenario: submit prompt intent

- **WHEN** an operator submits an intent with raw prompt text
- **THEN** an intent is created with `kind: prompt`, generated id starting with `int-`,
  initial version 1, empty acceptance list, empty run_ids, and persisted to `.aisdlc/intents/<id>/current.json`

#### Scenario: submit reference intent without network calls

- **WHEN** an operator submits an intent with a reference such as `github:#2395` or `jira:PROJ-1`
- **THEN** the intent is created with `kind: reference`, source system matching the prefix and ref matching the target, without performing external network calls

#### Scenario: intent versioning and ledger privacy event

- **WHEN** an intent is submitted
- **THEN** version file `.aisdlc/intents/<id>/versions/1.json` is created matching `current.json`, and an evidence ledger event is appended containing id, kind, source, and actor, but omitting the intent text

#### Scenario: optional store mirror hook no-op

- **WHEN** an intent is created and `aisdlc.store` is not installed or importable
- **THEN** intent creation succeeds without error and does not fail due to the missing store

#### Scenario: secrets in intent text are refused

- **WHEN** an operator submits an intent whose text or spec contains a value matching the factory SECRET_VALUES pattern (e.g., `ghp_`, `sk-`, `AKIA`, `Bearer`)
- **THEN** the kit raises a `KitError` with code `literal_secret` and stores nothing

#### Scenario: unknown reference system is rejected

- **WHEN** an operator submits a reference intent with a system prefix other than `github`, `jira`, `url`, or `file`
- **THEN** the kit raises a `KitError` and stores nothing

### Requirement: Spec Markdown parsing

The kit SHALL parse Markdown specification documents into structured intent data:
the title SHALL be extracted from the first level-1 heading (`# Title`) or a `## Title`
section; the description SHALL be extracted from the text body preceding criteria;
acceptance criteria SHALL be extracted from checkboxes (`- [ ]`) or bullet items under
an Acceptance Criteria heading; references SHALL be extracted from Markdown links (a
label and a target URL) or bullet lines, with reference `kind` inferred as
`architecture`, `security`, `adr`, or `standard` if found in the label or URL,
defaulting to `other`.

#### Scenario: parse markdown spec into title, description, criteria, and typed references

- **WHEN** a markdown spec file containing title, description, acceptance checkboxes, and typed markdown links is submitted
- **THEN** the intent has `kind: spec`, parsed `title`, populated `acceptance[]` list, and `references[]` list with correctly classified kinds

#### Scenario: spec file with missing title gets a derived title

- **WHEN** a markdown spec file has no level-1 heading and no `## Title` section
- **THEN** the kit derives the title from the first non-empty line of the description text and uses it as the intent title

### Requirement: CLI submit, list, and show

The CLI SHALL extend the existing `aisdlc intent` command with `submit`, `list`, and `show`
subparsers. `submit` SHALL accept mutually exclusive inputs `--text`, `--ref <system:id>`,
or `--file <spec.md>`, with optional `--title` and `--json`. `list` SHALL output all
persisted intents (`--json` supported). `show <id>` SHALL retrieve and display a single
intent by id (`--json` supported). Existing subcommands `start`, `end`, `tool`, and `run`
SHALL remain functional and unmodified.

#### Scenario: CLI submit text, ref, or file

- **WHEN** `aisdlc intent submit` is invoked with `--text "Do something"` or `--ref github:#1` or `--file spec.md`
- **THEN** the intent is created, persisted, and its summary or JSON payload is displayed

#### Scenario: CLI list intents

- **WHEN** `aisdlc intent list --json` is executed
- **THEN** a JSON array of all stored intents is output to stdout

#### Scenario: CLI show intent by id

- **WHEN** `aisdlc intent show int-12345678 --json` is executed for an existing intent
- **THEN** the complete stored intent payload is returned as JSON

### Requirement: API endpoints for intents

`aisdlc serve` SHALL expose `GET /factory/intents` (read scope), `GET /factory/intents/{id}`
(read scope), and `POST /factory/intents` (write scope, `X-AISDLC-CSRF` matching token,
server-derived actor). `POST /factory/intents` SHALL accept `{text}`, `{ref}`, or
`{spec: markdown}` with optional `title`, returning HTTP 201 Created and the created intent.

#### Scenario: GET /factory/intents returns intent list

- **WHEN** an authenticated client sends `GET /factory/intents` with read scope
- **THEN** the response is 200 OK with a list of intent summaries

#### Scenario: GET /factory/intents/{id} returns intent details

- **WHEN** an authenticated client sends `GET /factory/intents/{id}` for an existing intent
- **THEN** the response is 200 OK with the full intent representation

#### Scenario: POST /factory/intents creates intent with write scope, CSRF, and actor

- **WHEN** an authenticated client sends `POST /factory/intents` with write scope, `X-AISDLC-CSRF`, and body `{"text": "Refactor auth"}`
- **THEN** the response is 201 Created with the persisted intent, and `created_by` reflects the authenticated actor

### Requirement: POST /factory/runs binds intent

`POST /factory/runs` SHALL accept an optional `intent` field specifying an intent id.
If provided, the kit SHALL verify that the intent exists (returning 404 Problem if not found),
bind the intent id to the Run payload, and append the generated run id to the intent's
`run_ids` array, saving a new version of the intent.

#### Scenario: POST /factory/runs accepts valid intent id and appends to run_ids

- **WHEN** `POST /factory/runs` is invoked with an existing intent id
- **THEN** the created Run record includes the `intent` field, and the intent's `run_ids` contains the new run id

#### Scenario: POST /factory/runs rejects nonexistent intent id

- **WHEN** `POST /factory/runs` is invoked with an intent id that does not exist
- **THEN** the call fails with HTTP 404 and code `not_found`, and no Run is created

### Requirement: MCP server and tools

The kit SHALL provide `aisdlc mcp` which runs a stdio JSON-RPC 2.0 MCP server.
The server SHALL implement standard MCP lifecycle methods `initialize` (declaring protocolVersion,
serverInfo, and tools capability), `notifications/initialized`, `tools/list`, and `tools/call`.
The server SHALL expose tools `intent_submit` (`text`, `ref`, or `spec`, optional `title`),
`intent_show` (`id`), and `factory_status` (reporting compact intent and run counts and latest
run status). Tool outputs SHALL be returned as MCP content `[{type: "text", text: <json>}]`,
and tool errors SHALL be marked with `isError: true`. Standard JSON-RPC errors -32600,
-32601, -32602, and -32700 SHALL be returned for invalid requests.

#### Scenario: stdio MCP initialize and list tools

- **WHEN** an MCP client sends `initialize` and then `tools/list` over stdio
- **THEN** the server returns server capabilities and lists tools `intent_submit`, `intent_show`, and `factory_status`

#### Scenario: MCP intent_submit tool

- **WHEN** an MCP client calls `tools/call` for `intent_submit` with arguments `{"text": "Test intent"}`
- **THEN** the tool creates the intent and returns its JSON representation in a text content block

#### Scenario: MCP intent_show tool

- **WHEN** an MCP client calls `tools/call` for `intent_show` with argument `{"id": "<existing-id>"}`
- **THEN** the tool returns the intent JSON representation in a text content block

#### Scenario: MCP factory_status tool

- **WHEN** an MCP client calls `tools/call` for `factory_status`
- **THEN** the tool returns compact status with counts of intents, runs, and latest run id/status

### Requirement: Uniform intent representation across entry points

The service functions `submit`, `list`, and `show` in `framework/ai-sdlc/aisdlc/intent_model.py`
SHALL be the sole implementation invoked by CLI, HTTP API, and MCP tools. Given identical
intent inputs, CLI, API, and MCP entry points SHALL persist and return identical intent
entities.

#### Scenario: same intent produced from CLI, API, and MCP

- **WHEN** identical input is submitted via CLI `intent submit`, HTTP `POST /factory/intents`, and MCP `intent_submit`
- **THEN** each entry point produces an intent with identical schema, normalized source, acceptance criteria, and reference categorization
