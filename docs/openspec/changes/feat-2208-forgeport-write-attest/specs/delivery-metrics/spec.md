## Purpose
Lead-time and stage metrics. This delta only clarifies the ForgePort read vs
write boundary; lead-time behavior is unchanged.

## MODIFIED Requirements

### Requirement: Read-only ForgePort builds the lead-time sample
The kit SHALL provide a read-only ForgePort that, given an issue number, fetches issue.created_at and linked pull requests via the `gh` CLI (injectable runner) and builds the same sample document shape used by the #2138 fixture. The lead-time path SHALL NOT create, update, or close issues/PRs, SHALL NOT create or update check runs, and SHALL NOT write `factory/gate` attestations. Approval-gated `factory/gate` attest / scoped write SHALL live on a separate `forge attest` surface (#2208) and SHALL NOT be reachable from `metrics lead-time`.

#### Scenario: Sample from forge runner
- **WHEN** ForgePort is given issue 2182 and a runner that returns a closed issue plus one linked PR with checks_green_at
- **THEN** the sample document includes that issue number, created_at, and the PR with linked_issues containing 2182

#### Scenario: Read-only surface
- **WHEN** ForgePort lead-time sample is built
- **THEN** the runner is invoked only with read subcommands (`issue view`, `pr view`, `pr list`, `api` GET/GraphQL query) and never with write verbs (create/edit/close/merge/comment)
