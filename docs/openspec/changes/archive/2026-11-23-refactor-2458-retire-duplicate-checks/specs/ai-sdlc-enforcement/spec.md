## ADDED Requirements

### Requirement: Notaire's commit, TDD and context checks run through aisdlc

Preflight and `sdlc-process.yml` SHALL run `aisdlc commits`, `aisdlc tdd` and `aisdlc budget` with Notaire's profile,
which carries Notaire's commit types (including `design`, the `[#n]` squash prefix and `Revert "…"`), its test paths
(including `docs/tests/` and `infra/tests/`), test-before-code order and the `sdlc-exception` waiver.
The standalone scripts that duplicated these checks SHALL NOT exist; `workspace/tests/test_scripts_layout.py`
lists them as removed.

#### Scenario: Notaire's commit forms pass

- **WHEN** `aisdlc commits` runs with Notaire's profile over `design: x`, `[#1210] docs(constitution): y (#1214)` and `fix!: z`
- **THEN** it exits 0, and it exits 1 for `update stuff` and `[#1210] update stuff (#1214)`

#### Scenario: Notaire's TDD rules hold

- **WHEN** `aisdlc tdd` runs with Notaire's profile over a production commit made before its test
- **THEN** it exits 1 naming the commit, and it exits 0 when the PR carries `sdlc-exception`

#### Scenario: the duplicate scripts are gone

- **WHEN** the repository is checked out
- **THEN** none of the three scripts exists and no workflow, preflight step or gate calls them
