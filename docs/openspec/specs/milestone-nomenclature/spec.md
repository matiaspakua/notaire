# milestone-nomenclature Specification

## Purpose
Defines how every Notaire milestone is named, dated, described and closed, so the board reads as history, shipped cuts, current train and future trains (#1847).

## Requirements

### Requirement: Open release trains are version first

Every open delivery milestone SHALL be titled `v<MAJOR>.<MINOR> · <Theme> (<YYYY-MM>)`, one per minor, with a due date equal to the end of its window.

#### Scenario: The current trains

- **WHEN** the policy lists the scheduled milestones
- **THEN** they are v0.3, v0.4, v0.5, v0.6 and v1.0 in that order, each matching the pattern

### Requirement: History is named by stage

Closed history milestones SHALL be titled `H<n> · <Stage> (<window>)` from `H0 · Swing monolith (2014–2018)` to `H8 · Pre-release hardening (2026-09–2026-10)`, or `H · <quarter> cohort: <theme>` for close-date cohorts.

#### Scenario: Priority buckets merge into H8

- **WHEN** a closed issue sits on the former v1.0, v1.1 or Backlog priority bucket
- **THEN** its target milestone is `H8 · Pre-release hardening (2026-09–2026-10)`

### Requirement: Shipped cuts own the issues they shipped

A closed issue that is unmilestoned, on the Q4 cohort or on a delivery train, and that closed inside a cut window SHALL belong to that cut's closed train.

#### Scenario: Closed between the v0.1 and v0.2.3 tags

- **WHEN** a closed issue on the Q4 cohort closed on 2026-10-11
- **THEN** its target is `v0.2 · Release identity & product forms (2026-10)`

#### Scenario: Curated history is kept

- **WHEN** a closed issue inside a cut window sits on `H8`
- **THEN** it keeps `H8`

### Requirement: Backlog and Catalog are continuous

`Backlog · Unscheduled` and `Catalog · Requirements & use cases` SHALL have no due date, and RF/RNF/CU anchors without work labels SHALL target Catalog.

#### Scenario: A use-case anchor

- **WHEN** an open issue is labeled only `CASO-DE-USO`
- **THEN** its target is `Catalog · Requirements & use cases` and the milestone payload has no due date

### Requirement: Descriptions follow one template

Every train and stage description SHALL start with `Goal:` and contain `Window:`, `Key deliverables:` and `Exit:`.

#### Scenario: Template check

- **WHEN** the policy descriptions are read
- **THEN** every train and stage description contains the four parts

### Requirement: Merged-away milestones and stale views do not linger

The apply run SHALL delete a milestone whose title is a rename source once its target exists and it holds no issue, and `--check` SHALL count each such milestone still present and each project view outside the view contract, except an old view name the run will still rename (#1847).

#### Scenario: Empty bucket merged into H8

- **WHEN** `History · v1.1 priority bucket` and H8 both exist and the bucket is empty
- **THEN** the apply run deletes the bucket, and `--check` reports it until then

#### Scenario: Bucket before its target exists

- **WHEN** the target milestone does not exist yet
- **THEN** the bucket is renamed, not retired

#### Scenario: Pre-taxonomy view

- **WHEN** the project has `Casos de Uso` or `RNF`, or `KANBAN` beside `Kanban`
- **THEN** `--check` reports each as a view to delete; `KANBAN` alone is not reported, because the run renames it
