<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. CU76. -->

## ADDED Requirements

### Requirement: Issue state, Project Status, and workflow labels stay coherent

Agents and the SDLC harness SHALL keep GitHub Issue `state`, Delivery Board
Project Status (Todo / In Progress / Done), and workflow labels coherent for
work items in scope. Project Status options today are Todo, In Progress, and
Done only — there is no Blocked column; blocked work MUST use the `blocked`
label. This rule belongs to CU76 and Constitution Gate 5 (#1550).

#### Scenario: CLOSED issue left In Progress fails

- **WHEN** an issue is `CLOSED` and Project Status is In Progress (or Todo / empty)
- **THEN** the coherence check fails and requires Status Done

#### Scenario: CLOSED Done without WIP labels passes

- **WHEN** an issue is `CLOSED`, Status is Done, and labels exclude `in-progress`
- **THEN** the coherence check passes for that item

#### Scenario: CLOSED keeps in-progress label fails

- **WHEN** an issue is `CLOSED` and still carries the `in-progress` label
- **THEN** the coherence check fails

#### Scenario: OPEN In Progress requires in-progress label

- **WHEN** an issue is `OPEN` with Status In Progress and no `in-progress` label
- **THEN** the coherence check fails

#### Scenario: OPEN In Progress with label passes

- **WHEN** an issue is `OPEN` with Status In Progress and the `in-progress` label
- **THEN** the coherence check passes

#### Scenario: in-progress label without In Progress Status fails

- **WHEN** an issue is `OPEN`, carries `in-progress`, and Status is Todo (or Done)
- **THEN** the coherence check fails

#### Scenario: OPEN blocked must not be Done

- **WHEN** an issue is `OPEN`, carries the `blocked` label, and Status is Done
- **THEN** the coherence check fails

#### Scenario: Done prefers CLOSED when AC met

- **WHEN** Status is Done and the issue is still `OPEN`
- **THEN** the coherence check fails (prefer CLOSED + Done when work is complete)

### Requirement: Process harness validates board coherence

Process Checks and local preflight SHALL invoke
`python3 workspace/sdlc/check-board-status-label-coherence.py` for PR-linked
issues (`--scope pr`). When `gh` is not authenticated or `GH_TOKEN` /
project access is unavailable, the check SHALL skip with an explicit message
and MUST NOT silently treat the board as verified. Foreman board hygiene MAY
use `--scope wip` or `--scope all`.

#### Scenario: Skip without auth

- **WHEN** `SKIP_BOARD_COHERENCE=1` is set or `gh` auth is unavailable
- **THEN** the checker exits 0 and prints a skip message

#### Scenario: Process Checks and preflight wire the checker

- **WHEN** Process Checks / preflight mapping is inspected
- **THEN** they reference `check-board-status-label-coherence.py`

### Requirement: Cloud AI SDLC docs require board Status sync

Permanent Cloud AI SDLC documentation under
`docs/300-development/304-ai-sdlc-cloud/` and the board contract under
`docs/github/README.md` SHALL instruct agents to sync Issue state, Project
Status, and labels before merge/close and during board hygiene, and SHALL
name the mechanical checker.

#### Scenario: Constitution and merge-gate docs name the gate

- **WHEN** an agent reads CONSTITUTION Gate 5 / tooling map or the Cloud merge-gate doc
- **THEN** the documents name board Status/label coherence and the checker command
