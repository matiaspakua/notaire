<!-- Governed by CONSTITUTION.md. CU76, issue #2385. -->

## ADDED Requirements

### Requirement: Harness report validates adapter id against the known list

`aisdlc harness report` MUST validate the `--adapter` id against the list returned by `harness list`. When the id is not found, the command MUST exit 2 with a `KitError` (code `unknown_adapter`) whose message names the known adapter ids, and MUST NOT write a ledger record. A known adapter id MUST continue to work unchanged.

#### Scenario: an unknown adapter is refused

- **WHEN** `harness report --run <id> --event run.started --adapter fake-adapter` runs and `fake-adapter` is not in the harness list
- **THEN** the command exits 2, prints error code `unknown_adapter`, mentions the known adapter ids, and no ledger record is written

#### Scenario: a known adapter still works

- **WHEN** `harness report --run <id> --event run.started --adapter local-worker` runs and `local-worker` is in the harness list
- **THEN** the command exits 0, the ledger record is appended, and the run status advances to `running`
