<!-- Governed by CONSTITUTION.md. CU76, issue #2052. -->

## ADDED Requirements

### Requirement: Notaire's merge stage enforces owner-only paths and anchored evidence

Notaire's profile MUST list the policy, gate, harness and merge-path files as owner-only, every glob MUST match a tracked file, and the `merge` stage MUST run `aisdlc protected` and `aisdlc evidence verify` as required gates, with `aisdlc scope` and `aisdlc size` advisory at `pre-pr`.

#### Scenario: a Constitution change reaches the merge stage

- **WHEN** a PR modifies `CONSTITUTION.md`
- **THEN** the `merge` stage fails `aisdlc protected` and the Owner merges it

#### Scenario: a typo'd owner-only glob

- **WHEN** a `[protected]` glob matches no tracked file
- **THEN** the profile adoption test fails

### Requirement: Proposals can declare a write set

The proposal template and the scaffold MUST offer a `### Write set` section that `aisdlc scope` reads, and the scaffold MUST omit it when the spec gives none.

#### Scenario: scaffolded write set

- **WHEN** `spec.json` carries `write_set`
- **THEN** the scaffolded proposal lists each glob under `### Write set`

### Requirement: The agent protocol is written where agents read it

The workflow rule MUST describe the decision packet, deviations, bounded corrections, RED+PRESERVE tests, the risk line and the evidence anchor, and the PR template MUST ask for deviations, AI assistance, the review budget and `Risk:`.

#### Scenario: protocol present

- **WHEN** the rule and the PR template are read
- **THEN** each protocol element is present
