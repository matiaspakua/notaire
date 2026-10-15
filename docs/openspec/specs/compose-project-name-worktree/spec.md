# compose-project-name-worktree Specification

## Purpose
TBD - created by archiving change fix-1678-compose-project-name-worktree. Update Purpose after archive.
## Requirements
### Requirement: Dev start defaults Compose project name for observability

`workspace/stack/start.sh` MUST export `COMPOSE_PROJECT_NAME` as `notaire` when unset, so the application network name remains `notaire_notary-network` for the external attach used by `infra/observability`.

#### Scenario: unset project name

- **WHEN** an operator runs `bash workspace/stack/start.sh` without `COMPOSE_PROJECT_NAME` set
- **THEN** the script exports `COMPOSE_PROJECT_NAME=notaire`

#### Scenario: explicit override for parallel stacks

- **WHEN** an operator sets `COMPOSE_PROJECT_NAME` to another value before start
- **THEN** that value is preserved and the default is not forced

#### Scenario: guard test

- **WHEN** `python3 infra/tests/test_dev_stack_isolation.py` runs
- **THEN** it asserts start.sh contains the notaire default export

