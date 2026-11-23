# aisdlc-production-line Specification

## Purpose

A machine-readable, ordered view of the factory's production line: every stage
declared in the profile (via `[[stages]]` or `[[gates]]`), with its gates, AI
agents, skills, tools/MCP, model, effort budget, inputs and outputs, plus a
guardrails block covering protected paths, hook-paths, the rules catalog, and
the sandbox/committer status.

Accessible via `aisdlc line [--json]` (CLI) and `GET /factory/line` (HTTP,
read scope).

## Requirements

### Requirement: Ordered stage list

`build_line(ctx)` SHALL return stages in the order defined by `profile.stages()`:
canonical lifecycle order first (intake, plan, red, green, pre-push, pre-pr,
merge, release), then alphabetically for any extra stage ids. A stage is included
if it appears in `[[stages]]` **or** has at least one gate declared in `[[gates]]`.

#### Scenario: line lists ordered stages with gates

- **GIVEN** a sample profile that declares stages `plan`, `red`, `pre-push`, and
  gates for `plan` and `red`
- **WHEN** `build_line(ctx)` is called
- **THEN** the returned `stages` list is ordered `plan` before `red` before
  `pre-push`, and each gate-bearing stage has a non-empty `gates` list

#### Scenario: stage with no gates still listed

- **GIVEN** a gate-only stage such as `green` that has a gate and no `[[stages]]`
  block
- **WHEN** `build_line(ctx)` is called
- **THEN** `green` appears in `stages` with its declared gate

The current profile loader rejects a `[[stages]]` id that has no corresponding
`[[gates]]` entry. Therefore a directly declared gate-less `[[stages]]` stage is not
loadable; the guard is preserved by the loader and the test uses the nearest
observable case, a gate-only stage.

### Requirement: Gate shape

Each gate in the `gates` list SHALL carry: `id` (stable slug derived from the
gate name), `name`, `command` (the `run` field), `required` (bool),
`mode` (`"required"` or `"advisory"`), `expect`, and `paths`.

#### Scenario: gate id is a stable slug

- **GIVEN** a gate with `name = "OpenSpec plans conform (live issue check)"`
- **WHEN** `build_line(ctx)` is called
- **THEN** the gate `id` is `"openspec-plans-conform-live-issue-check"`

### Requirement: inputs/outputs with profile override

Each stage in the payload SHALL carry `inputs` (list of strings), `outputs`
(list of strings), and `io_source` (`"profile"` or `"default"`).

When a `[[stages]]` block declares `inputs` and/or `outputs`, those values are
used and `io_source = "profile"`. When neither is declared, the kit provides
defaults for the seven known stage ids (plan, red, green, pre-push, pre-pr,
merge, release) and `io_source = "default"`. For unknown stage ids with no
declaration, `io_source = "default"` and both lists are empty.

#### Scenario: inputs/outputs from profile override kit defaults

- **GIVEN** a profile with a `[[stages]]` block for `plan` declaring
  `inputs = ["GitHub issue", "use case (CU)"]` and
  `outputs = ["OpenSpec change"]`
- **WHEN** `build_line(ctx)` is called
- **THEN** the `plan` stage entry has
  `inputs = ["GitHub issue", "use case (CU)"]`,
  `outputs = ["OpenSpec change"]`, and `io_source = "profile"`

#### Scenario: stage without inputs/outputs gets defaults and io\_source default

- **GIVEN** a profile with a `[[stages]]` block for `red` that declares no
  `inputs` or `outputs`
- **WHEN** `build_line(ctx)` is called
- **THEN** the `red` stage entry has non-empty `inputs` and `outputs` from the
  kit defaults and `io_source = "default"`

### Requirement: Guardrails block

The payload SHALL include a `guardrails` object with:

- `protected.owner` — the owner string from `[protected]`
- `protected.paths` — the list of protected path globs
- `hooks` — the subset of protected paths whose value contains `hook`
  (case-insensitive), as a list of strings, documented as hook-path entries
- `rules` — the entries from the `rules` catalog (empty list if the catalog
  dir is absent)
- `isolator` — the dict returned by `isolator.isolator_status()`

#### Scenario: guardrails expose protected paths, hooks, rules, isolator

- **GIVEN** a sample profile with `[protected]` paths that include at least
  one path containing the substring `hook`, and a rules catalog
- **WHEN** `build_line(ctx)` is called
- **THEN** `guardrails.protected.paths` is non-empty, `guardrails.hooks` lists
  only the hook-containing paths, `guardrails.rules` reflects the catalog, and
  `guardrails.isolator` is a dict

### Requirement: HTTP read scope

`GET /factory/line` SHALL return 200 with the production-line JSON payload using
the read scope (Bearer token with `read` or higher). The response SHALL include
`schema_version` consistent with other factory payloads.

#### Scenario: GET /factory/line read scope

- **GIVEN** `aisdlc serve` is running with a valid read-scope Bearer token
- **WHEN** `GET /factory/line` is requested with that token
- **THEN** the response is 200 JSON with `schema_version` and a `stages` array

### Requirement: CLI text and JSON output

`aisdlc line` (without `--json`) SHALL print a human-readable summary of stages,
their gates, and guardrails. `aisdlc line --json` SHALL print the full JSON payload
to stdout and exit 0.

#### Scenario: text and --json output

- **GIVEN** a valid profile with at least one stage and one gate
- **WHEN** `aisdlc line --json` is invoked
- **THEN** stdout is valid JSON containing `stages` and `guardrails` keys and exit
  code is 0
- **WHEN** `aisdlc line` is invoked (without `--json`)
- **THEN** stdout contains human-readable stage and gate information and exit
  code is 0

### Requirement: Stage metadata keys

Every stage in the `build_line(ctx)` output SHALL carry these keys — present on
every stage object, never omitted: `id`, `gates`, `agents`, `skills`, `mcp`,
`tools`, `model`, `effort`, `guardrails`, `inputs`, `outputs`, `io_source`.

Values when the stage has a `[[stages]]` block: taken from `stage_dicts(profile)`
for `agents`, `skills`, `mcp`, `model`, `effort`; `tools` equals `mcp`;
`guardrails` is the list of grants targeting the stage.

Values when the stage has NO `[[stages]]` block (gate-only stage):
`agents=[]`, `skills=[]`, `mcp=[]`, `tools=[]`, `model=null`, `effort={}`,
`guardrails=[]` (unless a `[[grants]]` entry targets the stage).

#### Scenario: stage always carries all keys with a configured stage

- **GIVEN** a profile with a `[[stages]]` block for `plan` declaring
  `agents = ["openspec-planner"]`, `skills = ["openspec-propose","openspec-update-change"]`,
  `mcp = ["repolens"]`, `model = "claude-sonnet-5-5-high"`,
  `effort = { tokens = 200000, time_seconds = 3600, questions = 20, review_lines = 400 }`,
  and `[[grants]]` with one entry targeting stage `plan`
- **WHEN** `build_line(ctx)` is called
- **THEN** the `plan` stage entry has all keys present:
  `agents = ["openspec-planner"]`, `skills = ["openspec-propose","openspec-update-change"]`,
  `mcp = ["repolens"]`, `tools = ["repolens"]`,
  `model = "claude-sonnet-5-5-high"`,
  `effort = { tokens = 200000, time_seconds = 3600, questions = 20, review_lines = 400 }`,
  and `guardrails` is a non-empty list containing the grant serialized with `"kind": "grant"`

#### Scenario: gate-only stage carries empty values

- **GIVEN** a profile with a gate for stage `green` but no `[[stages]]` block
  for `green`, and no `[[grants]]` entry targeting `green`
- **WHEN** `build_line(ctx)` is called
- **THEN** the `green` stage entry has:
  `agents = []`, `skills = []`, `mcp = []`, `tools = []`,
  `model = null`, `effort = {}`, `guardrails = []`

#### Scenario: stage guardrails list grants of the stage

- **GIVEN** a profile with two `[[grants]]` entries: one with `stage = "plan"`
  and one with `stage = "pre-push"`, and a `[[stages]]` block for `plan`
- **WHEN** `build_line(ctx)` is called
- **THEN** the `plan` stage's `guardrails` list contains exactly the grant
  with `stage = "plan"`, serialized via `grant_dict` with `"kind": "grant"`
  injected, and the `pre-push` stage's `guardrails` list contains exactly
  the grant with `stage = "pre-push"`

#### Scenario: real profile shape with agents, skills, model, effort and mcp

- **GIVEN** a profile matching the Notaire `.aisdlc/aisdlc.toml` shape:
  four `[[stages]]` blocks (`plan`, `red`, `pre-push`, `pre-pr`) each with
  agents, skills, mcp, model, and effort fields, plus `[[grants]]` entries
- **WHEN** `build_line(ctx)` is called
- **THEN** every stage with a `[[stages]]` block has non-empty `agents`,
  `skills`, `mcp`, `tools` (equal to `mcp`), a string `model`, and an
  `effort` object with the declared budget keys; stages without a
  `[[stages]]` block have the empty defaults
