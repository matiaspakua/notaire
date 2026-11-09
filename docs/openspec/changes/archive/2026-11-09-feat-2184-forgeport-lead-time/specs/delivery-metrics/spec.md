## ADDED Requirements

### Requirement: Read-only ForgePort builds the lead-time sample
The kit SHALL provide a read-only ForgePort that, given an issue number, fetches issue.created_at and linked pull requests via the `gh` CLI (injectable runner) and builds the same sample document shape used by the #2138 fixture. The port SHALL NOT create, update, or close issues/PRs, SHALL NOT create or update check runs, and SHALL NOT write `factory/gate` attestations.

#### Scenario: Sample from forge runner
- **WHEN** ForgePort is given issue 2182 and a runner that returns a closed issue plus one linked PR with checks_green_at
- **THEN** the sample document includes that issue number, created_at, and the PR with linked_issues containing 2182

#### Scenario: Read-only surface
- **WHEN** ForgePort lead-time sample is built
- **THEN** the runner is invoked only with read subcommands (`issue view`, `pr view`, `pr list`, `api` GET/GraphQL query) and never with write verbs (create/edit/close/merge/comment)

### Requirement: Live lead-time CLI uses ForgePort
`aisdlc metrics lead-time --issue N` SHALL compute lead time with the fixed event pair via ForgePort. `--fixture` SHALL remain available. Exactly one of `--issue` or `--fixture` is required. Missing/non-green linked PRs SHALL yield `no-evaluable` (not a pass).

#### Scenario: Live issue green
- **WHEN** `metrics lead-time --issue 99 --json` runs against a ForgePort sample with a green linked PR
- **THEN** status is ok and seconds equals checks_green_at minus issue.created_at

#### Scenario: Live issue no-evaluable
- **WHEN** ForgePort returns an issue with no green linked PR
- **THEN** the command exits non-zero with status no-evaluable
