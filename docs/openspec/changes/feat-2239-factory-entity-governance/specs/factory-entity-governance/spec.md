## ADDED Requirements

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

#### Scenario: happy path to approved
- **WHEN** a workflow is created and transitions define → request_validation → approve with honest approval
- **THEN** its governance state is `approved`

#### Scenario: illegal transition
- **WHEN** a draft workflow attempts `approve`
- **THEN** the command fails and state remains `draft`

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
