## ADDED Requirements

### Requirement: Triage restores values the harness derived

Before checking `triage.env`, the triage gate SHALL restore every key the harness
seeded with a real value among `ISSUE`, `USE_CASE` and `TYPE`, and log the repair.

#### Scenario: Derived value restored

- **WHEN** the seed has `TYPE=docs` and the worker wrote `TYPE=fix`
- **THEN** `triage.env` holds `TYPE=docs` and `TYPE` is reported as repaired

#### Scenario: Underivable value kept

- **WHEN** the seed has `TYPE=?` and the worker wrote `TYPE=fix`
- **THEN** `triage.env` keeps `TYPE=fix` and nothing is reported

### Requirement: Triage proofs must be able to fail

The triage gate SHALL reject a `command` proof whose program only searches or prints.

#### Scenario: Search command rejected

- **WHEN** a criterion reads `proven by: command bash grep -r X docs/`
- **THEN** the criterion is reported as not a proof

#### Scenario: Script command accepted

- **WHEN** a criterion reads `proven by: command bash scripts/preflight.sh`
- **THEN** nothing is reported

### Requirement: Promised tests need a tests phase

The triage gate SHALL reject `new test` proofs when `KIND` is not `code`.

#### Scenario: Docs kind with new test rejected

- **WHEN** `KIND=docs` and a criterion reads `proven by: new test XTest#shouldY`
- **THEN** the gate reports a KIND conflict

#### Scenario: Code kind with new test accepted

- **WHEN** `KIND=code` and a criterion reads `proven by: new test XTest#shouldY`
- **THEN** nothing is reported
