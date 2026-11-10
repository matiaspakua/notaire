## ADDED Requirements

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
