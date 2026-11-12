# factory-entity-governance Specification

## Purpose
TBD - created by archiving change feat-2239-factory-entity-governance. Update Purpose after archive.

## Requirements

### Requirement: Immutable entity revisions
The kit SHALL store factory entity content as immutable revisions with a current
pointer. An update SHALL allocate `version+1` and MUST NOT rewrite prior revision
bytes. Digests SHALL cover the revision payload (excluding mutable pointer-only
metadata).

#### Scenario: new version preserves prior bytes
- **WHEN** an entity is created then updated with a new summary
- **THEN** history lists both versions and version 1 file bytes are unchanged

### Requirement: Extended catalog kinds
Catalog kinds SHALL include `skills`, `mcp`, `agents`, `memory`, `workflows`,
`providers`, `integrations`, and `rules`. Unknown kinds SHALL fail closed.

#### Scenario: providers kind round-trip
- **WHEN** a provider entry is added with an `env:` credential ref and listed
- **THEN** the entry appears under kind `providers` and show redacts secret-shaped values

#### Scenario: unknown kind rejected
- **WHEN** catalog list uses kind `widgets`
- **THEN** the command exits with a configuration error

### Requirement: Workflow governance state machine
A workflow SHALL carry governance state in
`draft | defined | validated | approved`. Legal transitions are `define`
(draft→defined), `request_validation` (defined→validated), `approve`
(validated→approved), and documented demote/reopen pairs. Illegal transitions
SHALL fail closed. List/show/history MUST NOT execute workflow steps.
**Phase B:** any HTML/serve adapter SHALL expose only legal transitions for the
current state and MUST fail closed on illegal approve attempts (same guards as CLI).

#### Scenario: happy path to approved
- **WHEN** a workflow is created and transitions define → request_validation → approve with honest approval
- **THEN** its governance state is `approved`

#### Scenario: illegal transition
- **WHEN** a draft workflow attempts `approve`
- **THEN** the command fails and state remains `draft`

#### Scenario: UI respects illegal transition
- **WHEN** backoffice attempts `approve` on a draft workflow
- **THEN** the adapter fails and governance remains `draft`

### Requirement: Approve honesty
The `approve` transition SHALL require ApprovalPort verify success when
`approvals_live` is true, or an explicit documented Owner override path when
not live. Cosmetic badges without verify/override are forbidden. Agents MUST NOT
self-approve.

#### Scenario: approve fails closed without live approval or override
- **WHEN** ApprovalPort is not live and Owner override is omitted
- **THEN** `approve` fails and state stays `validated`

### Requirement: Approved-only runtime filter
The kit SHALL expose a list/filter of workflows whose governance state is
`approved` for later runtime selection. Non-approved workflows MUST NOT appear
in that filter.

#### Scenario: approved filter excludes draft
- **WHEN** one approved and one draft workflow exist
- **THEN** the approved filter returns only the approved id

### Requirement: OpenAPI and CLI alignment
OpenAPI 3.1 SHALL describe FactoryEntity, EntityVersion, Workflow, governance
states/transitions, and catalog write/list paths consistent with CLI JSON.
`aisdlc contract check` SHALL pass. List/show never execute MCP, agents, or
workflow steps.

#### Scenario: contract check green
- **WHEN** `aisdlc contract check` runs after Phase A schemas land
- **THEN** it exits 0 and the contract mentions Workflow and EntityVersion

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
