# factory-runtime-ui Specification

## Purpose
Kit-neutral factory **runtime** HTML/serve surface (Phase D / #2242): create a
project, bind repositories, select an approved workflow, start a run, and monitor
evidence-derived progress — distinct from the backoffice control plane.

## Requirements

### Requirement: Runtime shell with Projects, Start run, Monitor

The kit SHALL render a runtime HTML shell with navigation for **Projects**,
**Start run**, and **Monitor**. The brand signal SHALL remain kit-neutral
(**AI SDLC factory**) and MUST NOT present Notaire product chrome. The runtime
area SHALL remain separate from the backoffice right-nav catalog modules.

#### Scenario: runtime shell has Projects Start Monitor and kit brand

- **WHEN** an operator exports or serves the runtime HTML for a profile
- **THEN** the document includes nav for Projects, Start run, and Monitor, a
  kit-neutral factory brand, and does not contain Notaire product chrome markers

### Requirement: Create project binds profile root

The runtime SHALL let an operator create a Project with `id` and `profile_ref`
(file/profile root binding — not a SaaS tenancy database). Mutations SHALL call
Phase A.4 `projects` ports only.

#### Scenario: create project binds profile_ref

- **WHEN** an operator creates a project via the runtime serve adapter with a
  profile_ref
- **THEN** the project is persisted with that profile_ref and appears in the
  Projects list

### Requirement: Bind repositories with dry_run default

The runtime SHALL let an operator bind one or more repositories to a project
with ForgePort-honest refs (`id`, `forge`, `url`, `default_branch`, `mode`).
When `mode` is omitted, the binding SHALL default to `dry_run`.

#### Scenario: bind repo defaults dry_run

- **WHEN** an operator binds a repository without specifying mode
- **THEN** the stored repo has `mode` equal to `dry_run`

### Requirement: Approved-only workflow picker

The Start run workflow picker SHALL list only workflows whose governance state
is `approved`. Draft, defined, and validated workflows MUST be excluded from
production start options.

#### Scenario: approved-only workflow picker

- **WHEN** both an approved workflow and a draft workflow exist
- **THEN** the Start run HTML options include the approved workflow id and do
  not include the draft workflow id

### Requirement: Start run never paints running without harness events

Starting a run from the runtime UI SHALL create a RunRequest via A.4 `runs.start`
and display the evidence-derived status (`queued` or `requested`). The UI MUST
NOT show status `running` solely because the operator clicked Start.

#### Scenario: start run shows queued not running

- **WHEN** an operator starts a run against an approved workflow via the runtime
  adapter and no harness `run.started` event exists
- **THEN** the resulting run status shown is `queued` or `requested` and is not
  `running`

#### Scenario: unapproved start refused

- **WHEN** a client attempts to start a run against a non-approved workflow via
  the runtime adapter
- **THEN** the operation fails closed with `workflow_not_approved` (or equivalent
  error surface) and no RunRequest is accepted

### Requirement: Progress monitor from evidence

The Monitor view SHALL show Run status, steps, and evidence events for a selected
run. The empty / no-adapter state SHALL explain that no harness adapter has
reported. Skip MUST NOT be presented as pass. Cancel SHALL be honestly disabled
or unavailable until the Runner port supports it.

#### Scenario: monitor shows evidence events or empty no-adapter copy

- **WHEN** a run exists with or without harness events
- **THEN** the Monitor HTML shows the derived status and either the event list or
  an empty-state explanation that no adapter has reported

#### Scenario: cancel honestly disabled

- **WHEN** the runtime Monitor is rendered
- **THEN** cancel is marked unsupported / disabled with an honest reason and
  MUST NOT claim a successful cancel

### Requirement: Agent never commits or approves from runtime UI

The runtime HTML SHALL NOT offer commit or workflow-approve controls. Those
remain outside this surface (sole-committer / ApprovalPort axioms unchanged).

#### Scenario: no commit or approve affordances

- **WHEN** an operator views any runtime module HTML
- **THEN** the document does not include commit or approve action controls
