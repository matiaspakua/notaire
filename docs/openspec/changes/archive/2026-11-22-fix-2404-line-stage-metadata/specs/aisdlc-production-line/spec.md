# aisdlc-production-line — Stage Metadata Delta

> ADDED requirement: every stage in the line payload carries agents, skills, mcp,
> tools, model, effort, and guardrails — never missing keys.

## ADDED Requirements

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
