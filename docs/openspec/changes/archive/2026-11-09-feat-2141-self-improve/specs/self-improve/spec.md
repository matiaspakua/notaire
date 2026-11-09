## ADDED Requirements

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
The system SHALL compute a Wilson lower bound (95%) over clean stage passes and SHALL demote autonomy on the first incident (`ok=false` after a prior clean streak is sufficient to model demotion input).

#### Scenario: Autonomy requires sample and Wilson floor
- **WHEN** fewer than 30 evaluable stage runs exist
- **THEN** autonomy status is `no-evaluable`
- **WHEN** at least 30 clean passes yield Wilson lower bound ≥ 0.80 and no incidents
- **THEN** autonomy reports eligible for promotion (L3)
- **WHEN** any incident is recorded in the sample
- **THEN** autonomy is demoted (L1) regardless of raw pass rate
