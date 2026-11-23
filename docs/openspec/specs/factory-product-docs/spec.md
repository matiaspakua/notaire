# factory-product-docs Specification

## Purpose

TBD - created by archiving change docs-2251-factory-product-analysis. Update Purpose after archive.

## Requirements

### Requirement: Factory design record is permanent and linked

The repository SHALL hold `FACTORY-PRODUCT-ANALYSIS.md` and `FACTORY-UI-SPEC.md` under `docs/300-development/306-ai-sdlc-framework/`, linked from `FACTORY-ROADMAP.md` and `framework/ai-sdlc/README.md`, and the UI spec SHALL end with the three Owner decisions unanswered.

#### Scenario: Design docs are linked from roadmap and kit README

- **WHEN** an agent opens `FACTORY-ROADMAP.md` or the kit README
- **THEN** both design docs are linked and exist

#### Scenario: Owner decisions stay open

- **WHEN** an agent reads `FACTORY-UI-SPEC.md`
- **THEN** it finds three decision packets and the marker that they are unanswered

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
