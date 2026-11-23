## ADDED Requirements

### Requirement: Stage kind

Every stage in the `build_line(ctx)` output SHALL carry a `kind` field with value
`"agent"`, `"deterministic"`, or `"human"`. The profile `[[stages]]` block may
declare `kind` explicitly. When omitted, the kit applies a default heuristic:
`"agent"` when the stage lists agents, `"human"` when the stage id is `"merge"`,
otherwise `"deterministic"`. An unknown `kind` value is a profile validation error.

#### Scenario: declared kind is returned in the line

- **GIVEN** a profile with a `[[stages]]` block for `plan` declaring
  `kind = "agent"`
- **WHEN** `build_line(ctx)` is called
- **THEN** the `plan` stage entry has `kind = "agent"`

#### Scenario: default kind is "agent" when stage has agents

- **GIVEN** a profile with a `[[stages]]` block for `red` declaring
  `agents = ["testing-qa"]` but no `kind`
- **WHEN** `build_line(ctx)` is called
- **THEN** the `red` stage entry has `kind = "agent"`

#### Scenario: default kind is "human" when stage id is "merge"

- **GIVEN** a profile with a gate for stage `merge` and no `[[stages]]` block
- **WHEN** `build_line(ctx)` is called
- **THEN** the `merge` stage entry has `kind = "human"`

#### Scenario: default kind is "deterministic" for other stages

- **GIVEN** a profile with a `[[stages]]` block for `pre-push` with no agents
  and no `kind`
- **WHEN** `build_line(ctx)` is called
- **THEN** the `pre-push` stage entry has `kind = "deterministic"`

#### Scenario: invalid kind is a profile error

- **GIVEN** a profile with a `[[stages]]` block declaring `kind = "robot"`
- **WHEN** the profile is loaded
- **THEN** a `ProfileError` is raised mentioning the invalid kind

#### Scenario: stage_dicts includes kind

- **GIVEN** a profile with a `[[stages]]` block for `plan` declaring
  `kind = "agent"`
- **WHEN** `stage_dicts(profile)` is called
- **THEN** the returned row for `plan` has `kind = "agent"`

#### Scenario: gate-only stage with no agents gets deterministic default

- **GIVEN** a profile with a gate for stage `signal` and no `[[stages]]` block
- **WHEN** `build_line(ctx)` is called
- **THEN** the `signal` stage entry has `kind = "deterministic"`
