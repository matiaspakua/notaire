# Workflow diagrams and tracker visual polish

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1949 |
| Use Case | RNF-09 – Uso de colores en la GUI |
| Branch | `feat/1949_workflow-diagram-polish` |
| Gate 1 status | passed |

## Objetivo

Workflow viewer/hero empty and loading chrome still use ad-hoc dashed boxes and raw
skeletons, and node type badges use decorative ALL-CAPS. Wave 4 of epic #1941 aligns
them with EmptyState / LoadingState / ErrorState and design-system restraint.

## What Changes

- `WorkflowViewer` empty → `EmptyState`.
- `WorkflowHero` empty / not-found / loading → EmptyState / ErrorState / LoadingState.
- `WorkflowNodeBox` type badge drops `textTransform: uppercase`.
- Document in FRONTEND-DESIGN-SYSTEM; CHANGELOG; Vitest (extend viewer/hero tests).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Workflow chrome uses the shared design system | RNF-09, ADR-011 | Made explicit for viewer/hero |
| Empty and failed async surfaces invite a next action | #1941 / #1947 | Applied to workflow hero |

## Capabilities

### New Capabilities

- `ui-workflow-chrome`: empty/loading/error composition for workflow surfaces.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | yes | WorkflowViewer, WorkflowHero, WorkflowNodeBox, tests, docs |
| `frontend-swing` | no | – |
| `infra` / CI/CD | no | – |

### Surface area

- No API / Flyway / env. BREAKING: no.

### Architecture review

Follows ADR-011. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `FRONTEND-DESIGN-SYSTEM.md` | Note workflow chrome consumers |
| `CHANGELOG.md` | Feature entry |

## Out of Scope

- Rewriting React Flow layout algorithms
- Charts/reports wave
- Migrating every admin workflow editor panel copy
