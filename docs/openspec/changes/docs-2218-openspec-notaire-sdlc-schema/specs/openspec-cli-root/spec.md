## ADDED Requirements

### Requirement: OpenSpec CLI discovers notaire-sdlc from docs/

When the OpenSpec CLI runs with working directory `docs/`, `openspec schemas --json` SHALL list project schema `notaire-sdlc`, and `openspec new change <name> --schema notaire-sdlc` SHALL create `docs/openspec/changes/<name>/`. The repository MUST NOT place a real `openspec/` directory at the repository root (ADR-026). Agents MUST use either `bash workspace/sdlc/seed-openspec-change.sh <name> … --create` or `cd docs && openspec …` for Gate 1 scaffolding; skills and NOTAIRE-ADAPTATIONS MUST name that working path.

#### Scenario: schemas lists notaire-sdlc from docs cwd
- **WHEN** `openspec schemas --json` runs with cwd `docs/`
- **THEN** the JSON array includes an object whose `name` is `notaire-sdlc` and `source` is `project`

#### Scenario: repository root has no openspec folder
- **WHEN** the location guard test runs on a clean checkout
- **THEN** `openspec/` does not exist at the repository root and `docs/openspec/` holds config, schemas, specs, and changes

#### Scenario: seed --create remains the preferred agent path
- **WHEN** an agent needs a new notaire-sdlc change
- **THEN** permanent docs and the agent workflow / openspec-propose skills instruct `seed-openspec-change.sh … --create` (or `cd docs` before `openspec new change --schema notaire-sdlc`)
