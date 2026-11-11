# factory-backoffice-ui Specification

## Purpose

Kit-neutral backoffice HTML shell for factory control (Phase B / #2240): right
module nav and CRUD adapters for workflows, agents, and skills over Phase A
ports — not a Notaire product SPA.

## ADDED Requirements

### Requirement: Right-nav kit-neutral shell
The kit SHALL render a backoffice HTML shell with a **right-side** module
navigation for at least Workflows, Agents, and Skills. The brand signal SHALL
be kit-neutral (**AI SDLC factory** or equivalent kit name) and MUST NOT present
Notaire product chrome or Notaire product path branding inside the kit UI.

#### Scenario: shell has right nav and kit brand
- **WHEN** an operator exports or serves the backoffice HTML for a profile with catalogs
- **THEN** the document includes a right-side module nav, a kit-neutral factory brand, and does not contain Notaire product chrome markers

### Requirement: Workflows agents skills CRUD adapters
The backoffice SHALL provide list, detail, create, and edit surfaces for
workflows, agents, and skills. Mutations SHALL call Phase A catalog/workflow
ports (CLI/OpenAPI-aligned) only. The UI MUST NOT introduce a second config
database or silent overwrite of prior revisions.

#### Scenario: list and detail for agents and skills
- **WHEN** catalog entries exist for agents and skills
- **THEN** the backoffice HTML lists them and detail views show current fields (redacted)

#### Scenario: create and edit allocate Phase A versions
- **WHEN** an operator creates then edits an agent via the backoffice serve adapter
- **THEN** history shows version 1 and version 2 and prior revision bytes remain unchanged

### Requirement: Workflow governance UI honesty
The workflow module SHALL display current governance state and only the
transitions Phase A allows from that state. The UI MUST NOT offer a direct jump
to `approved` from `draft` or otherwise bypass ApprovalPort-honest approve
guards. Server-side Phase A enforcement remains authoritative.

#### Scenario: allowed transitions only
- **WHEN** a workflow is in `draft`
- **THEN** the workflow detail offers `define` and does not offer `approve`

#### Scenario: approve cannot skip guards
- **WHEN** a client attempts an illegal or unguarded approve through the backoffice adapter
- **THEN** the operation fails closed and governance state is unchanged

### Requirement: Version history visible
Entity detail views SHALL surface immutable revision history from Phase A
`history` so operators can see prior versions without mutating them.

#### Scenario: history listed on detail
- **WHEN** an entity has at least two revisions
- **THEN** the detail HTML includes both version numbers

### Requirement: Credentials never plaintext
Credential-shaped values and secrets SHALL be redacted in all backoffice HTML
and JSON payloads. Show/list MUST reuse Phase A redaction; plaintext secrets
MUST NOT appear in exported HTML.

#### Scenario: secret refs redacted in HTML
- **WHEN** a provider-like credential field or secret-shaped value exists on a shown entity
- **THEN** the HTML contains a redacted marker and MUST NOT contain the plaintext secret
