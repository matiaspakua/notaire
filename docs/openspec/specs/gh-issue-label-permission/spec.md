# gh-issue-label-permission Specification

## Purpose

Cloud agents and fleet docs must name the GitHub Issues write scope required to
add labels (`issues: write` / `addLabelsToLabelable`), and Gate 1.6 must honestly
SKIP when the ambient integration cannot label — never fake "in progress".

## Requirements

### Requirement: Document Issue label write scopes for Cloud agents

Permanent cloud fleet docs SHALL name the GitHub permission required to add Issue
labels: Repository · Issues **Read & write** (`issues: write`) and GraphQL
`addLabelsToLabelable`. Docs SHALL say where the Owner grants that capability
(Cursor Cloud / GitHub App installation, or `notaire-fleet` via `FLEET_APP_ID` and
`FLEET_APP_PRIVATE_KEY` + `fleet-token.sh`).

#### Scenario: docs name issues write and addLabelsToLabelable

- **WHEN** an agent or Owner reads `FLEET-IDENTITY.md` or `ENVIRONMENT-CHECKLIST.md`
- **THEN** the text mentions Issues write / `issues: write` and `addLabelsToLabelable`

#### Scenario: docs say where Owner grants label write

- **WHEN** ambient Cursor `gh` cannot label Issues
- **THEN** docs instruct the Owner to grant Cursor App Issues Read & write or configure
  notaire-fleet secrets on the cloud Environment card

### Requirement: Honest Gate 1.6 skip when label write is denied

Agents MUST run Gate 1.6 through `bash workspace/sdlc/add-issue-wip-label.sh <n>`.
When `gh` returns a permission error for `addLabelsToLabelable` / Issues write, the
helper SHALL print an honest **SKIPPED** note, exit 0 so Gate 1 continues, and MUST
NOT claim the Issue was moved to in progress. When labeling succeeds, it SHALL report
the WIP label applied (`status:in-progress` preferred).

#### Scenario: addLabelsToLabelable 403 is honest SKIP

- **WHEN** `gh issue edit --add-label` fails with Resource not accessible by
  integration / `addLabelsToLabelable`
- **THEN** `add-issue-wip-label.sh` exits 0, stdout/stderr contains SKIPPED, and does
  not print a success "labeled" / "moved to IN PROGRESS" claim

#### Scenario: successful label reports labeled

- **WHEN** the integration can write labels and `status:in-progress` (or legacy
  `in-progress`) exists
- **THEN** the helper exits 0 and reports the Issue was labeled with the WIP name

#### Scenario: agent workflow points at the helper

- **WHEN** an agent follows ai-agent-workflow skill/rule or a new notaire-sdlc tasks
  template for Gate 1.6
- **THEN** the instructed command is `add-issue-wip-label.sh`, with honest SKIP
  guidance on 403
