# factory-adopter-demo Specification

## Purpose
Prove factory UI reuse via a kit-neutral demo seed and a sandbox adopter path:
at least one approved workflow with pinned agents/skills used by Foreman stages,
documented commands + UI entry, independence intact (CU76 / #2243).

## Requirements

### Requirement: Demo seed creates an approved workflow on a temp adopter

The kit SHALL provide `aisdlc demo seed` that, on a profile with `[catalogs]`,
ensures agent and skill catalog entries exist, creates a workflow blueprint,
migrates the body to schema v2 with pinned refs, and advances governance to
`approved` (Owner override when ApprovalPort is not live). The seed SHALL NOT
execute agents or MCP. MCP refs on the demo workflow SHALL be empty.

#### Scenario: demo seed creates approved workflow on temp project

- **WHEN** an operator runs `aisdlc demo seed` against a temporary adopter profile
- **THEN** `workflow list --approved` includes the seeded workflow id and its
  governance is `approved`

#### Scenario: from-stages pins Foreman agent and skill ids

- **WHEN** an operator runs `aisdlc demo seed --from-stages` on a profile that
  declares `[[stages]]` agents and skills
- **THEN** the approved workflow body pins catalog refs whose ids are a subset of
  those stage agent and skill ids

#### Scenario: doctor catalog-validate and report stay green after seed

- **WHEN** demo seed completes on a temp adopter
- **THEN** `aisdlc doctor`, `aisdlc catalog validate`, and `aisdlc report` exit 0

### Requirement: Kit independence holds for the demo path

Kit `aisdlc/`, `bin/`, and `templates/` SHALL NOT mention adopter product path
markers. A temp-project demo SHALL still pass the kit independence scan after
seed assets exist only under the temp adopter root (not under kit source).

#### Scenario: kit independence holds after demo implementation

- **WHEN** the kit independence test scans `aisdlc/`, `bin/`, and `templates/`
- **THEN** there are no hits for adopter product path markers

### Requirement: Sandbox adopter demonstrates Foreman-aligned approved workflow

The Notaire sandbox adopter profile SHALL expose at least one `approved`
workflow whose pinned agent and skill catalog ids appear in Foreman
`[[stages]]` knobs already used by the fleet. Product admin UI SHALL NOT be
replaced by factory backoffice as part of this change.

#### Scenario: Notaire has approved workflow with stage refs

- **WHEN** the live Notaire profile catalogs are listed for approved workflows
- **THEN** at least one approved workflow exists and every pinned agent/skill id
  in its body appears in Foreman stage knobs

#### Scenario: Notaire doctor stays clean with demo catalogs

- **WHEN** `aisdlc doctor` runs against the Notaire profile after demo wiring
- **THEN** the command exits 0 with no new errors from the demo catalogs
