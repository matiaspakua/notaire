# kit-lifecycle Specification

## Purpose
Non-interactive aisdlc install, update, and uninstall for the AI SDLC kit. Update Purpose after archive.

## Requirements

### Requirement: Install copies kit and scaffolds without product trees
The kit SHALL provide `aisdlc install` that places the kit and profile scaffold, refuses destinations under frontend or backend-api or product OpenAPI paths, and exits non-interactively with documented codes.

#### Scenario: Happy path
- **WHEN** an empty repo runs install --path tools/ai-sdlc --name demo
- **THEN** the kit bin exists, aisdlc.toml exists, and frontend/backend-api trees are unchanged

#### Scenario: Forbidden path
- **WHEN** install --path frontend/ai-sdlc is requested
- **THEN** the command exits 2 and writes nothing under frontend

### Requirement: Update is idempotent and preserves adopter state
`aisdlc update` SHALL re-copy the kit without rewriting adopter profile notes or evidence.

#### Scenario: Double update
- **WHEN** update runs twice after install
- **THEN** both exit 0 and adopter profile edits remain

### Requirement: Uninstall is reversible without purge by default
`aisdlc uninstall` SHALL remove the kit and install manifest while leaving policy and evidence; `--purge` SHALL also remove scaffold and evidence.

#### Scenario: Default uninstall
- **WHEN** uninstall runs without --purge
- **THEN** kit is gone and CONSTITUTION.md plus evidence remain
