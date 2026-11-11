## ADDED Requirements

### Requirement: Adapter-neutral service layer

The kit SHALL expose workflow and catalog operations as functions that return dicts and raise `KitError(code, detail)` with a stable documented `code`, and SHALL NOT print. The CLI SHALL format their results and map `KitError` to the existing exit codes, leaving CLI text and JSON byte-identical.

#### Scenario: service returns data

- **WHEN** a service function for create, list, show, transition, catalog show or history succeeds
- **THEN** it returns a dict and writes nothing to stdout

#### Scenario: failure raises a typed error

- **WHEN** an operation fails (unknown id, illegal transition, duplicate, missing catalogs)
- **THEN** it raises `KitError` whose `code` is in `CODES` and whose `detail` is the former message

#### Scenario: CLI output is unchanged

- **WHEN** any `workflow list|show|create|transition` or `catalog list|show|history|add|update` invocation runs
- **THEN** stdout, stderr and exit code equal the goldens recorded from main
