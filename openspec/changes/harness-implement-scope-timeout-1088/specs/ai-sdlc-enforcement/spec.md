## ADDED Requirements

### Requirement: The implement scope admits planned files

The implement phase scope SHALL allow `.localai/`, the change's OpenSpec folder,
the adapter `source_roots`, and, as exact paths, every existing file listed in
triage `## Files to Edit` or in the change's traceability `## Planned Files`.

#### Scenario: Planned file outside the source roots

- **WHEN** traceability `## Planned Files` lists `CONSTITUTION.md` and the file exists
- **THEN** the implement scope matches `CONSTITUTION.md`

#### Scenario: Unplanned file

- **WHEN** `README.md` is outside the source roots and neither list names it
- **THEN** the implement scope does not match `README.md`

#### Scenario: Non-path cell

- **WHEN** a Planned Files cell holds a bare class name that is not an existing path
- **THEN** it adds nothing to the scope

### Requirement: The worker watchdog enforces the timeout

The harness SHALL run each worker in its own process group and SHALL end the
whole group when `WORKER_TIMEOUT` expires, with KILL if TERM does not stop it,
exiting 124.

#### Scenario: Worker finishes in time

- **WHEN** the worker exits with status 3 before the timeout
- **THEN** the watchdog exits 3

#### Scenario: Worker ignores TERM

- **WHEN** the worker and its child ignore TERM and outlive the timeout
- **THEN** both are killed and the watchdog exits 124
