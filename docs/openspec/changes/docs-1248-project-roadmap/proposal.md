# Project roadmap for managers and parallel agents

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1248 |
| Use Case | CU76 |
| Branch | `cursor/project-management-sdlc-566d` |
| Gate 1 status | passed |

## Objetivo

A manager cannot see how Notaire was organized or what is left, because most
issues have no milestone and the phase milestones named in the SAD were deleted.
Agents that pick the oldest open issue collide on the same module and spend
runs on the requirements catalog.

## What Changes

- Add a project-management guide under `docs/300-development/305-project-management/` with history cohorts, delivery trains, scope catalogs, label contract, lane rules, and the roadmap chart.
- Add a pure classifier and dry-run script (`workspace/sdlc/roadmap_classify.py`, `workspace/sdlc/organize-roadmap.py`) plus unit tests.
- Add the `project-manager` agent and `project-management` skill, and register them on the Cloud fleet.
- Publish the same chart on the GitHub Pages Docs tab.
- Point `docs/github/README.md`, the SAD phase table, and the fleet concurrency rules at this model.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Catalog requirements (RF, CU, RNF) stay open and are not delivery work | CU76, docs/github organization | Made explicit |
| Claimable work has one milestone, one priority, and one lane | CU76 | New |
| Two agents do not claim the same lane | CU76 | New |
| This change does not close stale issues one by one | #1248 acceptance | Made explicit — #1248 stays open |

## Capabilities

### New Capabilities

- `project-roadmap`: classification of an issue onto one milestone and one lane, with scope catalogs excluded from agent claims.

### Modified Capabilities

- None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | No API or schema change |
| `frontend` | no | Product UI unchanged |
| `frontend-swing` | no | Retired; not a target |
| `infra` / observability | no | No runtime change |
| CI/CD (`.github/workflows`) | no | No workflow edit |
| `docs` | yes | Project-management guide, SAD pointer, GitHub organization page |
| `github-page` | yes | Docs tab Roadmap chart |
| `workspace` | yes | Classifier, dry-run script, unit test |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway): none
- Configuration / `.env`: none
- Dependencies: none
- BREAKING for API clients: no
- Architecture review: process and documentation only. No ADR. The fleet manifest gains a role; module boundaries in ADR-026 are unchanged.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/305-project-management/` | New manager guide and snapshot |
| `docs/github/README.md` | Milestones and labels point at the guide |
| `docs/200-architecture/201-SAD/sad.md` | §11.3 drops links to deleted milestone numbers |
| `docs/300-development/304-ai-sdlc-cloud/` | Fleet role and concurrency |
| `docs/README.md`, `docs/300-development/README.md`, `docs/300-development/DEVELOPMENT-PLAN.md` | Navigation |
| `AGENTS.md` | On-demand row for the project manager |
| `CHANGELOG.md` | n/a — not a user-visible product change |

## Out of Scope

- Mutating GitHub milestones, labels, or project views from this agent (the token is read-only). Maintainer steps are in `ROADMAP.md`.
- Closing, deduplicating, or rewriting each open issue. That remains #1248.
- Implementing the Phase 4 use-case screens tracked by #898.
