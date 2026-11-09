## MODIFIED Requirements

### Requirement: Factory config is validated without executing plugins

`aisdlc config validate` MUST check the profile ceiling, trusted manifests, the
migration inventory, **and stage profile catalog refs when `[[stages]]` is
declared**. A single failure MUST make the command exit 1. Validation MUST NOT
execute plugins, MCP servers, or agents.

#### Scenario: partial failure

- **WHEN** one trusted manifest has the wrong id and another is valid
- **THEN** config validate exits 1

#### Scenario: unknown stage catalog ref fails validate

- **WHEN** a stage declares an agent id absent from the agents catalog
- **THEN** config validate exits 1 naming the unknown id

### Requirement: Config show is redacted and cannot raise the pull-request ceiling

`config show` MUST require `--redacted`. A request to set the open pull-request
cap above the profile value MUST exit 2. When stage knobs are declared, the
redacted view MUST include a `stages` field with agents/skills/mcp/model/effort
(never secrets from catalogs).

#### Scenario: ceiling

- **WHEN** the profile cap is 3 and the flag asks for 9
- **THEN** the command exits 2

#### Scenario: config show includes stages

- **WHEN** the profile declares stage knobs and `config show --redacted --json` runs
- **THEN** the JSON includes `stages` with those knobs
