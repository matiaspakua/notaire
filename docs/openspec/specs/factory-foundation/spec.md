# factory-foundation Specification

## Purpose
TBD - created by archiving change feat-2121-factory-foundation. Update Purpose after archive.

## Requirements

### Requirement: Factory config is validated without executing plugins

`aisdlc config validate` MUST check the profile ceiling, trusted manifests, the
migration inventory, stage profile catalog refs when `[[stages]]` is declared,
**and `[context].memory` pack ids when memory packs are declared**. A single
failure MUST make the command exit 1. Validation MUST NOT execute plugins, MCP
servers, agents, or memory packs.

#### Scenario: partial failure

- **WHEN** one trusted manifest has the wrong id and another is valid
- **THEN** config validate exits 1

#### Scenario: unknown stage catalog ref fails validate

- **WHEN** a stage declares an agent id absent from the agents catalog
- **THEN** config validate exits 1 naming the unknown id

#### Scenario: unknown context memory pack fails validate

- **WHEN** `[context].memory` declares a pack id absent from the memory catalog
- **THEN** config validate exits 1 naming the unknown id

### Requirement: Config show is redacted and cannot raise the pull-request ceiling

`config show` MUST require `--redacted`. A request to set the open pull-request
cap above the profile value MUST exit 2. When stage knobs are declared, the
redacted view MUST include a `stages` field with agents/skills/mcp/model/effort
(never secrets from catalogs). When `[context].memory` (or files/max_tokens) is
set, the redacted view MUST include a `context` field with those values.

#### Scenario: ceiling

- **WHEN** the profile cap is 3 and the flag asks for 9
- **THEN** the command exits 2

#### Scenario: config show includes stages

- **WHEN** the profile declares stage knobs and `config show --redacted --json` runs
- **THEN** the JSON includes `stages` with those knobs

#### Scenario: config show includes context memory

- **WHEN** the profile declares `[context].memory` and `config show --redacted --json` runs
- **THEN** the JSON includes `context.memory` with those pack ids

### Requirement: Plugin plan prints argv and does not run it

The OpenSpec adapter MUST print `openspec validate <change> --strict` for a single-token change name.

#### Scenario: dry plan

- **WHEN** plugin plan is called for openspec with change demo-change
- **THEN** the stdout is that argv line and the process is not started by the plan command

### Requirement: The migration inventory is not allowed to claim the move is finished

The inventory MUST contain at least one migrated row and one planned row. Product OpenAPI and frontend source MUST stay.

#### Scenario: honest inventory

- **WHEN** inventory check runs on the adopter profile
- **THEN** it exits 0 and the product rows are class stay

### Requirement: Migration inventory records extra harness stubs

The migration inventory MUST include discoverable harness-adapter rows for the
Claude Code, Codex, and ACP plugin stub directories under `framework/ai-sdlc/plugins/`,
and MUST still contain at least one planned row so it cannot claim the move is finished.

#### Scenario: inventory names the three stubs

- **WHEN** inventory check runs on the Notaire profile
- **THEN** it exits 0 and the three stub plugin paths are present as harness-adapter rows
