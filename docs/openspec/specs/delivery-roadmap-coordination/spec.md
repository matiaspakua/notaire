# delivery-roadmap-coordination Specification

## Purpose
TBD - created by archiving change chore-1569-delivery-roadmap-coordination. Update Purpose after archive.

## Requirements

### Requirement: Complete milestone coverage

The project SHALL assign every GitHub issue exactly one milestone. Work closed
before the migration SHALL use a closed history cohort. Work completed after
planning SHALL keep the delivery train in which it shipped.

#### Scenario: Closed issue coverage is complete

- **WHEN** the milestone policy check reads all repository issues
- **THEN** it reports zero closed issues without a milestone
- **AND** it does not move an issue that already belongs to a history or delivery milestone

#### Scenario: Open issue coverage is complete

- **WHEN** the milestone policy check reads all repository issues
- **THEN** it reports zero open issues without a milestone
- **AND** every open issue resolves to one of the six open delivery trains

### Requirement: Full-lifecycle project views

Project #1 SHALL expose a roadmap that spans closed history and future delivery
trains, a Kanban limited to claimable open work, and an agent queue using the
same non-catalog filter.

#### Scenario: Roadmap shows history through future planning

- **WHEN** a human or agent opens the full-lifecycle Roadmap
- **THEN** catalog anchors are excluded
- **AND** closed history and open delivery work are included
- **AND** every repository issue is present on Project #1
- **AND** the view is grouped by Milestone
- **AND** each included item has Start and Target dates derived from its milestone

#### Scenario: Kanban remains an operational queue

- **WHEN** a human or agent opens the Kanban
- **THEN** only open non-catalog delivery issues are shown
- **AND** the columns are Todo, In Progress, and Done as defined by Status
- **AND** blocked and readiness signals remain labels rather than columns

### Requirement: Deterministic multi-agent claim protocol

All Notaire agent fleets SHALL use the milestone, priority, derived lane,
Issue state, Project Status, and WIP labels as one coordination protocol.

#### Scenario: Agent selects the next issue

- **WHEN** a foreman selects work
- **THEN** it ignores catalog anchors, roadmap umbrellas, blocked work, and work needing information
- **AND** it selects the highest-priority item in the earliest open train whose derived lane is free
- **AND** it never selects owner-only work

#### Scenario: Agent claims and releases work coherently

- **WHEN** an agent claims an issue
- **THEN** the issue remains open, gains `in-progress`, and has Status In Progress
- **AND WHEN** the issue is closed
- **THEN** Status is Done and `in-progress` and `blocked` are absent

### Requirement: One durable project-management source

Permanent project-management documentation SHALL match the live milestone and
view model and SHALL be discoverable by humans and every agent entry point.

#### Scenario: Agent or human finds current guidance

- **WHEN** a reader starts from `AGENTS.md`, the cloud AI SDLC, or
  `docs/github/README.md`
- **THEN** the reader is directed to
  `docs/300-development/305-project-management/`
- **AND** milestone titles, view filters, and claim rules are stated once and
  linked elsewhere

### Requirement: Nine board views without label-mirroring fields

Project #1 SHALL be checked against nine views: Kanban, Current train, Agent queue, In flight, Needs triage, Blocked / needs info, Release roadmap, Shipped and Catalog. Their filters SHALL name each legacy label and its taxonomy name together (a comma is OR), and no view SHALL show the Priority, Area or Kind project fields, which labels replace (#1860).

#### Scenario: Agent queue lists only ready, unheld Todo work

- **WHEN** the Agent queue filter is read
- **THEN** it selects open Todo issues labeled `status:ready` or `ready-for-dev`, excludes both hold-label families and catalog anchors

#### Scenario: Filters survive the rename

- **WHEN** the catalog exclusion is read
- **THEN** it names `requerimiento-funcional`, `requerimiento-no-funcional`, `CASO-DE-USO`, `trace:rf`, `trace:rnf` and `trace:cu`

#### Scenario: No view uses a deleted field

- **WHEN** the visible fields of every view are read
- **THEN** none is Priority, Area or Kind
