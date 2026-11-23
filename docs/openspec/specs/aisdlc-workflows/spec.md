# aisdlc-workflows Specification

## Purpose

TBD - created by archiving change feat-2410-workflow-steps. Update Purpose after archive.

## Requirements

### Requirement: A draft workflow's steps are edited with their line stage

`aisdlc workflow edit --id <id> --steps-file <file>` and `PUT /factory/workflows/{id}` SHALL
replace a workflow's v2 `steps[]` and return it to draft. Each step SHALL have a unique `id` and a
`stage`; `agent`, `skills`, `mcp`, `gates`, `provider` and `budget` are optional; `on_fail` is
`stop` (default) or `continue`. An approved workflow SHALL refuse edits until it is demoted. The
API SHALL require write scope, the CSRF header and `If-Match` with the current version.

#### Scenario: Steps with stages are saved and the workflow returns to draft

- **WHEN** a validated workflow is edited with steps spec (stage plan) and red (stage red)
- **THEN** its body has those two steps with their stages, its version increases and its
  governance is draft

#### Scenario: A step without a stage is refused

- **WHEN** an edit has a step with no `stage`
- **THEN** the edit fails with `invalid_body` naming the step and nothing is written

#### Scenario: Duplicate step ids are refused

- **WHEN** an edit has two steps with id `red`
- **THEN** the edit fails with `invalid_body`

#### Scenario: An approved workflow is immutable

- **WHEN** an approved workflow is edited
- **THEN** the edit fails with `immutable_approved`

#### Scenario: The API needs If-Match with the current version

- **WHEN** `PUT /factory/workflows/{id}` is sent with a stale `If-Match`
- **THEN** it answers 409 `version_conflict` and nothing is written

### Requirement: Runs report the stage of their step

Once the approved workflow's steps name their stage, `GET /factory/status` SHALL report that
stage for the running step.

#### Scenario: Status shows the step's stage

- **WHEN** a run of a workflow whose step `green` has stage `pre-push` reports step `green` started
- **THEN** the status stage is `pre-push`
