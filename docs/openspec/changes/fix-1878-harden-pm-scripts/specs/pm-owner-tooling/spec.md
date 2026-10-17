## Purpose

Defines the contracts for Notaire Owner milestone/board tooling so apply and
check runs are complete, observable, and safe for multi-step PM work (CU76).

## ADDED Requirements

### Requirement: Whole-run create never sends due_on null
A simulated GitHub that rejects POST bodies containing `"due_on": null` MUST
accept every milestone create from `ensure_milestones`, and after
`assign_issues` and `retire_milestones` the residual mismatch count MUST be zero
for the fixture.

#### Scenario: Fake GitHub rejects due_on null on whole run
- **WHEN** a fake GitHub rejects any POST with `"due_on": null` and the policy
  WANTED set is applied via ensure / assign / retire
- **THEN** every create succeeds and the fixture ends with zero mismatches

### Requirement: Check reports full drift surface
`--check` MUST report missing policy milestones, description and due-date drift,
retired milestones still present, stale views, closed issues carrying `status:*`
labels, a per-(current, target) bucket summary, and committed snapshot freshness
versus live state.

#### Scenario: Check lists description and due drift
- **WHEN** a live milestone description or due date differs from policy
- **THEN** `--check` prints that milestone and increments the mismatch count

#### Scenario: Check lists closed issues with status labels
- **WHEN** a CLOSED issue still carries a `status:*` label
- **THEN** `--check` reports it

#### Scenario: Check fails on stale committed snapshot
- **WHEN** the committed `milestone-snapshot.json` disagrees with live counts or
  milestone titles
- **THEN** `--check` exits non-zero with a snapshot freshness message

### Requirement: Scope preflight before writes
`--apply` MUST verify the token has `repo` and `project` scopes before the first
mutating call and exit non-zero naming any missing scope.

#### Scenario: Missing project scope aborts before writes
- **WHEN** the token lacks `project`
- **THEN** apply exits before creating or patching milestones

### Requirement: Step headers and HTTP errors
Each apply step MUST print a header with remaining work count. HTTP failures MUST
name the step and the object (path or title).

#### Scenario: HTTP error names step
- **WHEN** a PATCH to a milestone fails
- **THEN** the error text includes the step name and milestone identity

### Requirement: Board loaders fill open_prs and R6 fires
Project loaders MUST set `BoardItem.open_prs` from linked open PRs. R6 MUST fail
when `status:in-review` and `open_prs == 0`.

#### Scenario: R6 fails on live-shaped data
- **WHEN** an OPEN item has `status:in-review` and `open_prs` is 0
- **THEN** `evaluate_item` returns an R6 violation

### Requirement: Scope all uses paginated GraphQL
`--scope all` MUST load items via paginated GraphQL without per-issue `gh issue
view` calls and finish in under 60 s on ~950 items when the API responds normally.

#### Scenario: All-scope loader uses GraphQL pagination
- **WHEN** `--scope all` runs
- **THEN** the loader pages GraphQL project items and does not call `fetch_issue`
  per row

### Requirement: Sync Status Done for closed issues
A repo tool MUST set Project Status to Done for CLOSED issues that are not Done,
with a unit test against a fake.

#### Scenario: Closed non-Done becomes Done
- **WHEN** the sync tool runs on a CLOSED item with Status In Progress
- **THEN** it issues the Status Done mutation (or dry-run prints it)

### Requirement: View layout contract and UI-only docs
Group-by / sort / column expectations for all nine views MUST live next to
`VIEW_CONTRACTS`. Settings the API cannot read MUST be listed in
`docs/github/README.md` as UI steps. Current train MUST be derived or documented.

#### Scenario: View contract documents group expectations
- **WHEN** a reader inspects `VIEW_LAYOUT` (or equivalent)
- **THEN** each of the nine views has documented group/sort/column expectations

### Requirement: Closes guard for multi_pr and Owner-pending
Process Checks MUST fail a PR that uses `Closes`/`Fixes` for an issue whose
OpenSpec change declares `multi_pr: true` or an Owner-pending marker; `Refs` MUST
pass.

#### Scenario: Closes blocked on multi_pr change
- **WHEN** a PR body contains `Closes #N` and the active change for #N has
  `multi_pr: true`
- **THEN** the guard exits non-zero recommending `Refs`

### Requirement: Owner scripts versioned and syntax-checked
Owner scripts under `workspace/sdlc/owner/` MUST default to preview, and CI MUST
run `bash -n` (and shellcheck when installed) on them.

#### Scenario: Owner scripts pass bash -n
- **WHEN** CI Process Checks run
- **THEN** every `workspace/sdlc/owner/*.sh` passes `bash -n`
