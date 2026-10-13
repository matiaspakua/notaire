<!-- Governed by CONSTITUTION.md. CU76, issues #1623 and #1622. Evidence 2026-10-13: four merges in one afternoon (#1621, #1626, #1620, #1624) each left an active change that failed validate-sdlc-plan on every open PR; 22 of 32 deltas could not archive. -->

## ADDED Requirements

### Requirement: The implementing PR archives its OpenSpec change

A pull request that adds or edits an active OpenSpec change SHALL archive it before merge, unless the change declares `multi_pr: true` in its `.openspec.yaml`. Archiving a change in the PR SHALL NOT fail the task-completion gate.

#### Scenario: Touched change left active fails

- **WHEN** a PR edits an active change without `multi_pr: true` and does not archive it
- **THEN** the archive hygiene check fails and tells the author to archive it in this PR

#### Scenario: Change archived in the PR passes

- **WHEN** a PR moves its change to `changes/archive/`
- **THEN** the check passes

#### Scenario: Multi-PR change may stay active

- **WHEN** the touched change sets `multi_pr: true`
- **THEN** the check does not require it to be archived

### Requirement: Another change's leftover does not block a PR

On a pull request, an active change whose issue is closed SHALL be reported as a warning, both by the archive hygiene check and by plan validation of all changes. Inventory mode SHALL fail on it.

#### Scenario: Leftover only warns on a pull request

- **WHEN** a PR does not touch an active change whose issue is CLOSED
- **THEN** the hygiene check passes and prints the leftover as a warning

#### Scenario: Leftover fails in inventory mode

- **WHEN** `check-openspec-archive-hygiene.py --inventory-only` finds that change
- **THEN** it exits non-zero

### Requirement: Delta operations match the main spec at Gate 1

Plan validation SHALL fail when an `ADDED` requirement already exists in the main spec, or when a `MODIFIED`, `REMOVED` or `RENAMED` requirement or its spec does not exist.

#### Scenario: MODIFIED on a missing spec fails

- **WHEN** a delta declares `MODIFIED` for a capability with no main spec
- **THEN** `check-delta-ops.py` reports it and `validate-sdlc-plan.sh` fails

#### Scenario: ADDED on an existing requirement fails

- **WHEN** a delta declares `ADDED` for a requirement already in the main spec
- **THEN** the check reports it

### Requirement: Process checks also run on main

`sdlc-process.yml` SHALL run the process self-tests, plan validation and the strict OpenSpec inventory on every push to `main`, daily, and on demand, while the PR-only checks stay on pull requests.

#### Scenario: Main health runs on push, schedule and dispatch

- **WHEN** the workflow triggers are read
- **THEN** they include `push` to `main`, a `schedule` and `workflow_dispatch`, and the `main-health` job runs the self-tests and `--inventory-only`
