# Production-line diagram on the Factory Control Overview

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2407 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/2407_production_line_diagram` |
| Gate 1 status | passed |

## Objetivo

The Overview lists the line as stacked cards, so nobody can see at a glance how the factory is working: where the run is, what each station is, which gates hold and what it costs. The Owner asked (2026-11-22) for a production-line diagram in the visual language of the factory deck's workflow slide, applied to this diagram only.

## What Changes

- The Overview's station cards become a diagram: a rail with one station per line stage, a token at the running station that moves with live events, a kind marker per station (AI agent, deterministic step, human decision), gates hanging under each station with their last evidence state, a correction-loop arc from pre-push back to red, and per-station throughput and cost from telemetry.
- An intent progress strip above the line: the run's intent (or that none is attached), stations completed of total as a segmented bar, elapsed time, cost so far and the current step.
- A detail panel for the selected station: kind, agents, skills, MCP and tools, model, effort, inputs, outputs, grants and a gates table.
- The approved `factory-feature` workflow gets a new version whose steps name their line stage, so runs map onto stations.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A gate that was skipped or left to do is shown as not evaluable, never as passed | CU76; AI SDLC "skip is not pass" | Made explicit |
| Every value on the diagram comes from the kit; a missing value is named, never shown as zero | CU76; dashboard DESIGN.md "Evidence, not theatre" | Made explicit |
| A station's state is always given in words, never by color alone | CU76; WCAG AA (ui-ux-design rules) | Made explicit |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `factory-dashboard-production-line`: the Overview line becomes a diagram with live token, station kinds, gate evidence states, correction loop, throughput and an intent progress strip.

## Impact Analysis

### Módulos afectados

<!-- Mark every module this change touches. Delete rows that do not apply, but do
     not delete the table: an empty table means the analysis was not done. -->

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — (product app untouched) |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `framework/ai-sdlc/factory-dashboard` | yes | Overview diagram, line model, styles, copy, tests |
| `.aisdlc` workflow catalog | yes | `factory-feature` version with stage-mapped steps |

### Surface area

<!-- Entities, endpoints, database schema (Flyway migration needed?), configuration,
     external dependencies. Name the endpoints and the migration version explicitly.
     State whether any change is BREAKING for API clients. -->

- Entities: none.
- Endpoints: none new; the dashboard reads the existing kit `GET /factory/line`, `/status`, `/report`, `/telemetry`, `/workflows/{id}` and `/events` through its proxy. Not breaking.
- Database (Flyway `V{n}`): none.
- Configuration / `.env`: a new `factory-feature` workflow version in `.aisdlc/catalogs/workflows/`.
- Dependencies: none.

### Write set

- `framework/ai-sdlc/factory-dashboard/src/ui/**`
- `framework/ai-sdlc/factory-dashboard/tests/**`
- `framework/ai-sdlc/factory-dashboard/DESIGN.md`
- `framework/ai-sdlc/factory-dashboard/README.md`
- `framework/ai-sdlc/CHANGELOG.md`
- `.aisdlc/catalogs/workflows/factory-feature/**`
- `docs/openspec/changes/**`
- `docs/openspec/specs/factory-dashboard-production-line/**`

### Architecture review

Follows ADR-030 in the dashboard: views call the existing API helpers; the pure line model lives in `src/ui/line-model.ts` with no React. No ADR: the change is presentation inside one view.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/factory-dashboard/DESIGN.md` | A "Production-line diagram" section: deck-derived tokens, kinds, gate states, motion, responsive rules |
| `framework/ai-sdlc/factory-dashboard/README.md` | Overview description |
| `framework/ai-sdlc/CHANGELOG.md` | `[Unreleased]` entry with (#2407) |

## Out of Scope

- Restyling any other dashboard screen (Owner decision: diagram only).
- A kit-declared station kind; the dashboard derives it (agents present, or the merge decision) until a follow-up issue adds a `kind` field.
- Per-run gate results; gates show the last evidence state across runs from `/factory/report`.
