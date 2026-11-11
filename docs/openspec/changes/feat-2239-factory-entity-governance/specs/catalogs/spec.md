## MODIFIED Requirements

### Requirement: Catalog CRUD
The kit SHALL provide CLI CRUD for catalog entries across kinds
`skills|mcp|agents|memory|workflows|providers|integrations|rules` as JSON with
digests. Updates SHALL create a new immutable revision (current pointer advances)
rather than silently overwriting prior revision bytes.

#### Scenario: add list remove
- **WHEN** an entry is added then listed then removed
- **THEN** list reflects each state and files match

#### Scenario: update creates new revision
- **WHEN** an existing entry is updated
- **THEN** history includes the prior revision unchanged and current points at version+1

### Requirement: Unknown kinds fail closed
Unknown catalog kinds SHALL exit with a configuration error.

#### Scenario: widgets
- **WHEN** catalog list --kind widgets
- **THEN** exit code is 2

## ADDED Requirements

### Requirement: Catalog show and history
The kit SHALL support show (current entity JSON) and history (revision list) for
catalog entries without executing entry payloads.

#### Scenario: show returns current version
- **WHEN** catalog show is invoked for an existing id
- **THEN** JSON includes id, kind, version, and digest
