## ADDED Requirements

### Requirement: Foreman dispatch plan from stage knobs
The system SHALL provide `aisdlc foreman plan [--stage <id>] [--json]` that resolves a
read-only Foreman dispatch plan from profile `[[stages]]` knobs (`agents`, `skills`,
`mcp`, `model`, `effort`). `aisdlc foreman route` SHALL be an alias of `plan`. The plan
MUST NOT execute agents, skills, or MCP servers and MUST NOT claim Isolator OS or
sandbox isolation. Omitted or empty stage knobs SHALL yield an empty `stages` list
(gates still run); the CLI MUST NOT invent stages.

#### Scenario: omitted stage knobs yield empty plan
- **WHEN** a profile has gates but no `[[stages]]` tables and `foreman plan --json` runs
- **THEN** the command exits 0 with `stages` as an empty list, `executes` false, and
  `sandbox` false

#### Scenario: mapped stage appears in plan json
- **WHEN** a profile declares `[[stages]]` for an existing gate stage with agents/skills/model/effort
  and `foreman plan --json` runs
- **THEN** the JSON `stages` array includes that stage with the declared knobs and
  `mapped_count` ≥ 1

#### Scenario: stage filter narrows the plan
- **WHEN** `foreman plan --stage plan --json` runs on a profile with a `plan` stage knob
- **THEN** every returned stage has `id` equal to `plan`

#### Scenario: unknown stage filter fails closed
- **WHEN** `foreman plan --stage not-a-gate` runs and that id is not a declared gate stage
- **THEN** the command exits 2 with an error naming the unknown stage

#### Scenario: plan never executes agents or mcp
- **WHEN** a stage references MCP/agent catalog ids and `foreman plan --json` runs
- **THEN** the command exits 0, prints honesty fields (`note` denying execution /
  Isolator / sandbox, `executes=false`, `sandbox=false`), and does not execute the
  referenced payloads

#### Scenario: route is an alias of plan
- **WHEN** `foreman route --json` runs on a profile with mapped stage knobs
- **THEN** the JSON shape matches `foreman plan --json` (same stages and honesty fields)
