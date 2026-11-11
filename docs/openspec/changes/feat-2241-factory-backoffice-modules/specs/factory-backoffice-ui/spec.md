# factory-backoffice-ui Delta — Phase C (#2241)

## Purpose

Extend the Phase B kit-neutral backoffice shell with remaining catalog modules
and an evidence-backed runs monitor. HTML/serve remain adapters over Phase A
catalogs and evidence/`report` — not a second SoR.

## ADDED Requirements

### Requirement: Remaining catalog module CRUD adapters
The backoffice SHALL provide list, detail, create, and edit surfaces for catalog
kinds **mcp** (Tools/MCP), **providers**, **memory** (Knowledge), **rules**
(Rules/Gates), and **integrations**. Mutations SHALL call Phase A `catalogs`
ports only. Edits SHALL allocate new immutable revisions. The right-side module
nav SHALL include these modules in the FACTORY-UI-SPEC order after Skills and
before Runs, with kit-neutral labels.

#### Scenario: nav lists Phase C catalog modules
- **WHEN** an operator exports the backoffice HTML
- **THEN** the right module nav includes Tools/MCP, Providers, Knowledge,
  Rules/Gates, and Integrations (in addition to Workflows, Agents, Skills)
  and does not contain Notaire product chrome markers

#### Scenario: list and detail for mcp and providers
- **WHEN** catalog entries exist for mcp and providers
- **THEN** the backoffice HTML lists them under their modules and detail views
  show redacted current fields plus version history

#### Scenario: create and edit allocate versions for knowledge
- **WHEN** an operator creates then edits a memory (Knowledge) entry via the
  backoffice serve adapter
- **THEN** history shows version 1 and version 2 and prior revision bytes remain
  unchanged

### Requirement: Runs monitor from evidence and report
The Runs module SHALL present a read/monitor view assembled from the evidence
ledger and the same summarize/report/cost data used by `aisdlc report`
(TelemetryPort/cost hooks inclusive). The UI MUST NOT introduce a parallel
metrics store. Cancel and retry controls SHALL be offered only when Runner/Forge
ports honestly report support; otherwise the module SHALL remain read-only with
honest empty/disabled actions and an explicit reason (A.4 Run entities pending
is an acceptable reason).

#### Scenario: runs monitor shows evidence summary
- **WHEN** the evidence ledger contains stage runs and optional cost entries
- **THEN** the Runs module HTML includes run counts and cost/report fields from
  evidence summarize (not a separate metrics database)

#### Scenario: cancel and retry honestly empty when unsupported
- **WHEN** cancel/retry are not proven by Runner/Forge ports
- **THEN** the Runs HTML exposes no enabled cancel/retry action that claims
  success and includes a reason that the actions are unavailable

#### Scenario: runs mutations rejected
- **WHEN** a client POSTs a create/edit/cancel/retry action for the runs module
  through the backoffice serve adapter
- **THEN** the operation fails closed without writing a parallel run store

### Requirement: Credentials never plaintext on provider modules
Credential-shaped values on providers (and other Phase C kinds) SHALL remain
redacted in all backoffice HTML. Plaintext secrets MUST NOT appear in exported
or served HTML.

#### Scenario: provider secrets redacted
- **WHEN** a providers entry contains an api_key or password-shaped field
- **THEN** the providers detail HTML contains a redacted marker and MUST NOT
  contain the plaintext secret

## MODIFIED Requirements

### Requirement: Right-nav kit-neutral shell
The kit SHALL render a backoffice HTML shell with a **right-side** module
navigation for Workflows, Agents, Skills, Tools/MCP, Providers, Knowledge,
Rules/Gates, Integrations, and Runs. The brand signal SHALL remain kit-neutral
(**AI SDLC factory**) and MUST NOT present Notaire product chrome.

#### Scenario: shell has right nav and kit brand
- **WHEN** an operator exports or serves the backoffice HTML for a profile with catalogs
- **THEN** the document includes a right-side module nav spanning Phase B and
  Phase C modules, a kit-neutral factory brand, and does not contain Notaire
  product chrome markers
