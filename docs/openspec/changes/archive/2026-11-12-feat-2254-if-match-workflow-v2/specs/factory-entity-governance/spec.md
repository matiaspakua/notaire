## ADDED Requirements

### Requirement: Workflow body v2 with pinned refs

Workflow entities SHALL support body schema v2: `steps[]` with fields
`id, stage, agent, skills[], mcp[], provider, gates[], budget{tokens,
time_seconds,usd}, on_fail`, plus `inputs[]` and `outputs[]`. References SHALL
use the form `<kind>:<id>@<version>`. v1 id-list bodies remain readable until
migrated. CLI create MAY continue to emit v1 until an explicit v2 write or
migrate.

#### Scenario: workflow body v2 shape

- **WHEN** a workflow is stored at `schema_version=2`
- **THEN** its `body` contains `steps`, `inputs`, and `outputs` arrays and step
  refs use `<kind>:<id>@<version>`

#### Scenario: request_validation pins digests

- **WHEN** `request_validation` succeeds on a v2 workflow
- **THEN** every ref is resolved at its pinned version and digests are recorded
  under `validation.pins`

#### Scenario: approve fails on pin digest drift

- **WHEN** a pinned catalog entry's digest no longer matches the digest recorded
  at validation
- **THEN** `approve` fails with a stable kit error and governance stays
  `validated`

#### Scenario: new workflow version returns to draft

- **WHEN** a workflow body is edited through `new_version`
- **THEN** the new revision has `governance=draft`

### Requirement: If-Match writes and actor audit

Catalog updates and the CLI update path SHALL require the expected entity
version. Mismatch SHALL raise a version-conflict error exposing the current
version. Every write and transition SHALL record `created_by` as
`{channel}:{identity}` derived by the server/adapter; no request body field may
set it.

#### Scenario: If-Match mismatch yields conflict

- **WHEN** an update supplies an expected version that is not the current version
- **THEN** the write is refused with code `version_conflict` and the current
  version is reported

#### Scenario: two writers cannot lose an update

- **WHEN** two writers each load version N and the first commits N+1
- **THEN** the second writer using expected version N is refused and must reload

#### Scenario: created_by is server-derived

- **WHEN** a write or transition completes
- **THEN** `created_by` equals the adapter-derived channel and identity and any
  client-supplied `created_by` in the body is ignored
