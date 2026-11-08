# factory-foundation Specification

## Purpose
TBD - created by archiving change feat-2121-factory-foundation. Update Purpose after archive.

## Requirements

### Requirement: Factory config is validated without executing plugins

`aisdlc config validate` MUST check the profile ceiling, trusted manifests, and the migration inventory. A single failure MUST make the command exit 1.

#### Scenario: partial failure

- **WHEN** one trusted manifest has the wrong id and another is valid
- **THEN** config validate exits 1

### Requirement: Config show is redacted and cannot raise the pull-request ceiling

`config show` MUST require `--redacted`. A request to set the open pull-request cap above the profile value MUST exit 2.

#### Scenario: ceiling

- **WHEN** the profile cap is 3 and the flag asks for 9
- **THEN** the command exits 2

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
