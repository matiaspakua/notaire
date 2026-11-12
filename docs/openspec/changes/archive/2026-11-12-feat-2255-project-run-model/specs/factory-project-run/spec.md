## Purpose

File-backed Project and Run entities with evidence-derived status and a harness
reporting contract so Phase D runtime UI can stay honest (CU76 / #2255).

## ADDED Requirements

### Requirement: Project binds profile and repos with dry_run default

The kit SHALL persist Projects with `id`, `profile_ref`, and `repos[]` where
each repo has `id`, `forge`, `url`, `default_branch`, and `mode` in
`dry_run|live`. When `mode` is omitted on create or add-repo, the kit SHALL
default to `dry_run`.

#### Scenario: project create defaults dry_run

- **WHEN** an operator creates a project with a repo that omits `mode`
- **THEN** the stored project repo has `mode` equal to `dry_run`

### Requirement: Start run requires approved workflow and appends RunRequest

Starting a run SHALL refuse any workflow whose governance is not `approved`
with stable code `workflow_not_approved`. On success the kit SHALL append a
`run.request` (RunRequest) evidence ledger event bound to the workflow
`id`, `version`, and `digest`, and SHALL persist a Run whose initial status
is not `running`.

#### Scenario: start run requires approved workflow

- **WHEN** an operator starts a run against a draft or validated workflow
- **THEN** the call fails with `workflow_not_approved` and no RunRequest is written

#### Scenario: start run appends RunRequest evidence

- **WHEN** an operator starts a run against an approved workflow version+digest
- **THEN** the evidence ledger contains a `run.request` event for that run id
  and the Run status is `queued` or `requested` (never `running`)

### Requirement: Status is evidence-derived only

Run `status` SHALL be derived only from evidence events for that run. The kit
start path SHALL never set `running`. Only harness-appended `run.started`,
`step.started`, `step.finished`, and `run.finished` events MAY advance a run
beyond `queued`. A step recorded as `skip` SHALL never be treated as `pass`.
A step budget overrun SHALL mark the run `blocked`.

#### Scenario: status stays queued without harness events

- **WHEN** a RunRequest exists and no harness `run.started` event is present
- **THEN** showing the run reports `queued` or `requested`, never `running`

#### Scenario: harness report moves run to running then finished

- **WHEN** the local-worker harness adapter appends `run.started` then `run.finished` ok
- **THEN** the derived status becomes `running` then `succeeded`

#### Scenario: budget overrun marks blocked; skip is never pass

- **WHEN** a harness adapter reports a step finished with `skip` or a budget overrun
- **THEN** skip is not recorded as pass, and budget overrun yields run status `blocked`

### Requirement: Evidence verify covers run events

`aisdlc evidence verify` SHALL accept well-formed chained ledger records that
include the new run event kinds without breaking the hash chain.

#### Scenario: evidence verify covers new run events

- **WHEN** a ledger contains `run.request` and harness `run.*` / `step.*` events
- **THEN** `aisdlc evidence verify` exits 0 and reports a chained head
