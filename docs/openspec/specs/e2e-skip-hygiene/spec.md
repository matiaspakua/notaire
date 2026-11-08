# e2e-skip-hygiene Specification

## Purpose

TBD - created by archiving change test-1243-e2e-skipped-scenarios. Update Purpose after archive.

## Requirements

### Requirement: Scenarios whose UI exists are executed

The search scenarios for deeds, document types and concepts and the budget template scenario MUST run and pass.

#### Scenario: searches run

- **WHEN** the TS-0020 spec runs against the stack
- **THEN** CU62, CU65 and CU66 search scenarios pass and are not skipped

#### Scenario: template action runs

- **WHEN** the CU39 scenario runs
- **THEN** the budget template action is exercised and passes

### Requirement: Remaining skips are documented gaps

The inventory guard MUST list exactly the remaining unconditional skips and the mapping document MUST give each a reason and an issue.

#### Scenario: inventory matches the sources

- **WHEN** test_e2e_reliability.py runs
- **THEN** the counts equal the unconditional test.skip calls and the total is 3
