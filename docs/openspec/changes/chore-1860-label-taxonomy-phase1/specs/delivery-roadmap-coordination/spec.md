## ADDED Requirements

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
