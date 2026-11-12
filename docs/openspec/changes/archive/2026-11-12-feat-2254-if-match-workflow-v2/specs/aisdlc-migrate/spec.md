## ADDED Requirements

### Requirement: Entity schema_version and migrate command

Every catalog entity SHALL carry an integer `schema_version`. The kit SHALL
provide `aisdlc migrate` that upgrades workflow entities from body schema v1 to
v2 idempotently and records the migration in the evidence ledger.

#### Scenario: entity carries schema_version

- **WHEN** a catalog entry is created or a new revision is written
- **THEN** the stored payload includes `schema_version` (default `1` for new
  non-migrated entries)

#### Scenario: migrate upgrades workflow v1 to v2

- **WHEN** `aisdlc migrate` runs against a profile whose workflows still use the
  v1 id-list body
- **THEN** each such workflow is rewritten to body v2 with `schema_version=2`
  and pinned `<kind>:<id>@<version>` refs derived from current catalog versions

#### Scenario: migrate is idempotent and records evidence

- **WHEN** `aisdlc migrate` runs a second time on an already-migrated profile
- **THEN** no workflow body changes and the evidence ledger still contains a
  migrate record documenting the run (including a no-op result)
