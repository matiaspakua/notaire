## ADDED Requirements

### Requirement: Empty friction is no-evaluable
When `aisdlc improve propose` finds zero friction items in the scanned evidence ledger, the command SHALL report `status=no-evaluable` and SHALL NOT write a proposal file under `.aisdlc/proposals/`.

#### Scenario: Empty ledger writes nothing
- **WHEN** the evidence ledger has no failed-gate friction
- **THEN** propose exits without creating a proposal JSON and reports no-evaluable

#### Scenario: Friction still writes a proposal
- **WHEN** the ledger has failed gate runs
- **THEN** propose still writes a proposal artifact with status proposed
