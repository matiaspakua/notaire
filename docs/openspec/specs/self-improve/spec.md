# self-improve Specification

## Purpose
TBD - created by archiving change feat-2141-self-improve. Update Purpose after archive.

## Requirements

### Requirement: Proposal-only self-improvement loop
The system SHALL provide `aisdlc improve propose` that reads the evidence ledger, emits a proposal artifact under `.aisdlc/proposals/`, and SHALL NOT mutate protected paths, profile policy, or feature flags in place. The artifact SHALL declare `status=proposed`, `commits=false`, and a merge rule of `foreman` or `owner`.

#### Scenario: Failed gates become a proposal without writing protected paths
- **WHEN** the ledger has failed stage runs and the operator runs `aisdlc improve propose`
- **THEN** a proposal JSON is written under `.aisdlc/proposals/` and protected profile/policy files are unchanged

#### Scenario: Agent cannot approve its own improvement
- **WHEN** a proposal sets `approved_by` to an agent role (or `status` to approved/merged by agent)
- **THEN** `aisdlc improve validate` fails

#### Scenario: Protected path candidates require Owner merge
- **WHEN** a proposal lists a protected path among candidate paths
- **THEN** validate fails unless `merge_rule=owner`

### Requirement: Wilson autonomy score
The system SHALL compute a Wilson lower bound (95%) over **required** gate outcomes
from evidence ledger runs with `stage=signal` (the stage local preflight records).
Skip and todo statuses SHALL NOT count as passes and SHALL be excluded from the
Wilson trial count (skip ≠ pass). Autonomy level SHALL default to **L0** and SHALL
NOT report L3 (or L2) from this command. On the first incident (required gate fail
or an explicit `incident` flag on a counted run), autonomy SHALL demote to **L1**.

#### Scenario: Autonomy requires sample and Wilson floor
- **WHEN** fewer than 30 evaluable required-gate outcomes exist in `signal` runs
- **THEN** autonomy status is `no-evaluable` and level is 0
- **WHEN** at least 30 required-gate passes yield Wilson lower bound ≥ 0.80 and no incidents
- **THEN** autonomy reports L0-eligible (`status=ok`, `level=0`) — never level 3
- **WHEN** any required-gate fail or incident is recorded in the sample
- **THEN** autonomy is demoted (`status=demoted`, `level=1`) regardless of raw pass rate

#### Scenario: Skip does not inflate Wilson trials
- **WHEN** a signal run contains only skipped required=false or skip-status gates
- **THEN** those gates are not counted as successes or trials for autonomy

### Requirement: Empty friction is no-evaluable
When `aisdlc improve propose` finds zero friction items in the scanned evidence ledger, the command SHALL report `status=no-evaluable` and SHALL NOT write a proposal file under `.aisdlc/proposals/`.

#### Scenario: Empty ledger writes nothing
- **WHEN** the evidence ledger has no failed-gate friction
- **THEN** propose exits without creating a proposal JSON and reports no-evaluable

#### Scenario: Friction still writes a proposal
- **WHEN** the ledger has failed gate runs
- **THEN** propose still writes a proposal artifact with status proposed
