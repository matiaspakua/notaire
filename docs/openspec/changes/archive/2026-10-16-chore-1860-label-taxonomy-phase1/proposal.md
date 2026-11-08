# Label taxonomy, phase 1: readers accept old and new names

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1860 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1860_label_taxonomy_phase1` |
| Gate 1 status | passed |

## Objetivo

The Owner approved a prefixed label taxonomy (`type:`, `priority:`, `area:`,
`status:`, `concern:`, `trace:`, `source:`, `process:`, `release:`), a claim that
requires `status:ready`, three new coherence rules, nine board views, and deleting
the Priority/Area/Kind project fields. Renaming labels breaks every reader that
matches a literal name, so phase 1 makes every reader accept both names before
the Owner runs the rename.

## What Changes

- `workspace/sdlc/label_taxonomy.py`: the legacy → new table, `expand()`, `type_labels()`, `wip_label()`.
- `milestone_policy.py` lanes and priorities read through `expand()`; the view contract becomes the nine views with dual-name filters and no Priority/Area/Kind fields.
- `claim-issue.py` requires readiness, reads both hold and WIP names, writes the WIP name the repo has.
- `check-board-status-label-coherence.py` reads both names and adds R6–R8.
- `check-sdlc-exception.sh`, `check-tdd-evidence.sh`, `check-openspec-archive-hygiene.py` accept `process:sdlc-exception`.
- `apply_milestone_policy.py` no longer patches the deleted `phase:*` labels and targets `area:frontend` / `trace:cu` when present.
- LABELS.md, PARALLEL-AGENTS.md and docs/github/README.md describe the taxonomy, lifecycle and views.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Claim needs `status:ready` | Owner approval of the proposal (#1860) | New |
| R6–R8 | same | New |
| Labels, not project fields, carry priority/area/kind | same | Changed |

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `issue-claim-protocol`, `ai-sdlc-enforcement`, `delivery-roadmap-coordination` (ADDED requirements).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github/workflows`) | no | the workflows pass label names through to the scripts changed here |
| `workspace` | yes | sdlc scripts and tests |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Process tooling only; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/305-project-management/LABELS.md` | taxonomy, lifecycle, mapping |
| `docs/300-development/305-project-management/PARALLEL-AGENTS.md` | claim requires ready |
| `docs/github/README.md` | views and lifecycle |
| `CHANGELOG.md` | Changed entry |

## Out of Scope

- Running the renames (Owner script `notaire-labels-apply.sh`).
- Phase 2: removing legacy names from code, CONSTITUTION and agent docs (follow-up issue).
