## ADDED Requirements

### Requirement: Measured factory results are published

The repository SHALL publish the factory's results with numbers taken from the evidence ledger, the
pre-push logs, the RepoLens usage report or GitHub, each with its date and source. The README SHALL
state the enforced coverage floors and that `preflight.sh --full` runs the suites a branch touches.

#### Scenario: a reader checks a published number

- **GIVEN** the factory results note of 2026-11-23
- **WHEN** a reader looks up a step time or a gate time
- **THEN** the note names the source (ledger events or pre-push logs) it was measured from

### Requirement: The store's SQLite side files stay out of git

`open_store` SHALL list `factory.db`, `factory.db-wal`, `factory.db-shm` and `factory.db-journal` in
`.aisdlc/.gitignore`.

#### Scenario: a store is opened

- **GIVEN** a repository with no `.aisdlc/.gitignore`
- **WHEN** the store is opened
- **THEN** the ignore file lists the database and its three SQLite side files
