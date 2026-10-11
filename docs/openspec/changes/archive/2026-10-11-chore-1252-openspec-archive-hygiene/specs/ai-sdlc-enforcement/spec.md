<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. CU76. -->

## ADDED Requirements

### Requirement: Shipped OpenSpec changes leave the active set

After a change's implementing pull request is merged and the linked GitHub Issue
is closed, the repository SHALL NOT keep that change under
`docs/openspec/changes/<name>/` as an active tree. The change MUST be archived
under `docs/openspec/changes/archive/` with any delta requirements synced into
main specs when deltas exist. This rule belongs to CU76 (quality / methodology
RF #90 / #91) and Constitution Gate 5.

#### Scenario: Closed issue still has an active change

- **WHEN** an inventory check runs and an active change's linked Issue is CLOSED
  and no matching archive folder exists
- **THEN** the check fails and names the change and the Issue

#### Scenario: Archived after merge

- **WHEN** the same change has been moved under `docs/openspec/changes/archive/`
  with a dated prefix and deltas synced when present
- **THEN** the inventory check passes for that change

#### Scenario: Open issue WIP stays active

- **WHEN** the linked Issue is OPEN
- **THEN** the inventory check does not require archive of that change

### Requirement: Implementation task checkboxes reflect shipped work

For an OpenSpec change touched by a pull request, the process gate SHALL fail
when any task checkbox in mandatory groups 3 through 8 remains unchecked, unless
the pull request carries the `sdlc-exception` label. Groups 1–2 and 9–12 MAY
remain open until after merge. Agents MUST tick groups 3–8 when that work has
actually shipped, or archive the change instead of leaving a false resume signal.

#### Scenario: Unticked implementation task fails the gate

- **WHEN** a pull request modifies `docs/openspec/changes/foo/` and `tasks.md`
  still has an unchecked item under groups 3–8
- **THEN** the process check fails and names the change and at least one
  unchecked task id

#### Scenario: Implementation groups complete

- **WHEN** every checkbox under groups 3–8 is checked for that change
- **THEN** the task-completion check passes even if groups 9–12 remain open

#### Scenario: Exception label bypass

- **WHEN** the pull request has the `sdlc-exception` label and still has
  unchecked items under groups 3–8
- **THEN** the task-completion check passes and the change's traceability
  Exceptions section records the reason

### Requirement: Cloud merge-gate docs require OpenSpec lifecycle verification

Permanent Cloud AI SDLC documentation under
`docs/300-development/304-ai-sdlc-cloud/` SHALL instruct agents to verify
OpenSpec archive hygiene and task-completion status before merge, in addition to
heavy CI verification. The instruction MUST be findable from the merge-gate
document agents already use for CI false-green avoidance.

#### Scenario: Merge-gate document names archive verification

- **WHEN** an agent reads the Cloud CI merge-gate document
- **THEN** the document tells them to verify OpenSpec archive / task completion
  (or run the mechanical checker) before merge

#### Scenario: Missing instruction fails the doc guard

- **WHEN** the merge-gate document omits OpenSpec archive or task-completion
  verification
- **THEN** the documentation guard test fails
