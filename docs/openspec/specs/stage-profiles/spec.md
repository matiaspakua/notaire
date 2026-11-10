# stage-profiles Specification

## Purpose
Per-stage profile knobs declare which agents, skills, MCP allowlist ids, model,
and effort budgets apply to each gate stage — resolved against catalogs without
executing anything.

## Requirements

### Requirement: Optional stage knobs in the profile
The profile SHALL accept optional `[[stages]]` entries keyed by `id` matching a
declared gate stage. Each entry MAY declare `agents`, `skills`, `mcp` (lists of
ids), `model` (string), and `effort` (budget object). Effort units SHALL be
documented as: `tokens` (prompt+completion budget), `time_seconds` (wall clock),
`questions` (clarification budget), `review_lines` (changed-line review size).
Omitted or empty stage knobs SHALL be a no-op (gates still run).

#### Scenario: omitted stage knobs are no-op
- **WHEN** a profile has gates but no `[[stages]]` tables
- **THEN** `config validate` and `doctor` succeed without requiring stage knobs

#### Scenario: parse agents skills mcp model effort
- **WHEN** a profile declares `[[stages]]` with agents, skills, mcp, model, and effort
- **THEN** the kit loads those fields on the stage profile and surfaces them in
  `config show --redacted` / `stage list --json`

#### Scenario: unknown stage id fails load
- **WHEN** a `[[stages]]` id does not match any gate stage in the profile
- **THEN** profile load fails with a configuration error (exit 2)

### Requirement: Stage refs resolve against catalogs fail-closed
Agent, skill, and MCP ids declared on a stage SHALL resolve against `[catalogs]`
entries of the matching kind. Skill ids MAY also resolve via the `[skills] dir`
overlay (`source=skills-dir`). An unknown id SHALL make `aisdlc config validate`
and `aisdlc doctor` fail (never silent pass). When catalogs dir is unset/missing
and a stage declares agent or MCP ids (or skill ids not present in skills-dir),
validate/doctor SHALL error — never treat missing catalogs as success.

#### Scenario: unknown catalog id fails validate
- **WHEN** a stage references an agent id absent from the agents catalog
- **THEN** `config validate` exits non-zero naming the unknown id

#### Scenario: unknown id fails doctor
- **WHEN** a stage references an MCP id absent from the mcp catalog
- **THEN** `doctor` reports an ERROR and exits non-zero

#### Scenario: skills-dir overlay resolves skill id
- **WHEN** a stage references a skill id present only as a skills-dir folder with
  `SKILL.md` and no JSON catalog row
- **THEN** `config validate` succeeds for that skill ref

#### Scenario: missing catalogs dir with agent ref fails
- **WHEN** a stage declares an agent id and `[catalogs] dir` is unset
- **THEN** `config validate` exits non-zero (never silent pass)

### Requirement: Stage list and show never execute
`aisdlc stage list` / `stage list --json` and `config show --redacted` SHALL only
read profile and catalog metadata. They MUST NOT execute MCP servers, agent code,
or skill scripts.

#### Scenario: stage list json never executes
- **WHEN** a stage references an MCP entry whose summary/ref looks like a shell
  command and `stage list --json` runs
- **THEN** the command exits 0, prints the resolved knobs, and does not execute
  the MCP payload

#### Scenario: config show redacted surfaces stages
- **WHEN** `config show --redacted --json` runs with declared stage knobs
- **THEN** the JSON includes a `stages` array with the resolved knob fields

### Requirement: Notaire maps multiple Foreman stages with real catalog refs
Notaire's adopter profile SHALL declare `[[stages]]` knobs for at least four
distinct gate stages that already exist in `.aisdlc/aisdlc.toml` gates, including
`plan`, `red`, and `pre-pr`. Each agent id MUST exist under
`.aisdlc/catalogs/agents/`; each skill id MUST resolve via catalogs or the
`[skills] dir` overlay. MCP lists MUST NOT invent ids when the MCP catalog is
empty (omit or empty list). Model/effort values MUST be declarative routing
hints from the existing fleet (for example `fleet-manifest.yaml` defaults), not
executed by the kit.

#### Scenario: Notaire foreman plan maps at least four stages
- **WHEN** `aisdlc --profile .aisdlc/aisdlc.toml foreman plan --json` runs on the
  Notaire profile after this change
- **THEN** `mapped_count` is ≥ 4 and the `stages` array includes `plan`, `red`,
  and `pre-pr` with non-empty agents and/or skills drawn from real catalog or
  skills-dir ids

#### Scenario: Notaire stage knobs fail closed on unknown agent
- **WHEN** a Notaire `[[stages]]` row references an agent id absent from
  `.aisdlc/catalogs/agents/`
- **THEN** `aisdlc config validate` / `doctor` exits non-zero naming that id

### Requirement: Green stage knobs only when a green gate exists
A `[[stages]]` entry with `id = "green"` SHALL be declared only when the profile
also has at least one `[[gates]]` with `stage = "green"`. When Notaire has no
green gate (G3 verified expressed as `pre-push` / `pre-pr`), the profile MUST
NOT invent a green gate solely to raise `mapped_count`; permanent docs SHALL
state that omission.

#### Scenario: Notaire omits green knobs without a green gate
- **WHEN** Notaire's profile has no gate with `stage = "green"` and
  `foreman plan --json` runs
- **THEN** no stage object with `id` equal to `green` appears in `stages`, and
  FACTORY-ROADMAP (or equivalent permanent note) documents that G3 uses
  `pre-push` / `pre-pr` instead
