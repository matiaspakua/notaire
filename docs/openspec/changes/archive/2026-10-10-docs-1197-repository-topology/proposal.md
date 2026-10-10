# Challenge the multi-repository proposal and publish a staged, evidence-gated plan

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1197 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `docs/1197_repository_topology` |
| Gate 1 status | draft |

## Objetivo

Issue #1197 proposes splitting the monorepo into eight repositories. Several of its premises and its migration playbook do not hold against `main`, and it reverses Owner decisions recorded in #1190. Record the measured evidence and a staged, gated alternative so the Owner can decide with numbers.

## What Changes

- Adds `docs/200-architecture/202-ADR/ADR-024-repository-topology.md` (status Proposed): evidence table, options, proposed decision and a 17-point critique of the original proposal.
- Adds `docs/300-development/REPO-SPLIT-PLAN.md`: measures and targets, Phase 0 work inside the monorepo, gates for every extraction, phases for `notaire-infra`, the local AI engine and `notaire-testing`, a corrected extraction recipe, rollback, risks and the preconditions if the Owner still chooses eight repositories.
- Opens child issues #1257 to #1261 for Phase 0 and rewrites #1197 around the decision and measurable criteria.
- No production code, tests or configuration change.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A topology change is decided from measured evidence and gated, reversible steps | #1197, Constitution P4 and section 12 | Made explicit |

## Capabilities

### New Capabilities

- (none — `skip_specs: true`)

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| Docs | yes | one report |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Documentation only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-024-repository-topology.md` | new ADR (Proposed) |
| `docs/300-development/REPO-SPLIT-PLAN.md` | new plan |
| `docs/300-development/README.md`, `docs/200-architecture/202-ADR/README.md` | links |
| `CHANGELOG.md` | n/a - not user visible |
