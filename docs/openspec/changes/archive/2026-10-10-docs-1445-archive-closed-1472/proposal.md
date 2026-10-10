# Archive closed-issue OpenSpec trees after #1472 / #1471 / #1460 / #1470

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1445 |
| Use Case | CU76 |
| Branch | `cursor/chore-openspec-archive-closed-1472-cf98` |
| Gate 1 status | passed |

## Objetivo

`validate-sdlc-plan.sh` fails on main: active OpenSpecs still require OPEN #1408,
#1367, and #1466 (now CLOSED after merges). Also archive shipped Gate 1
`docs-1445-owner-phase0-evidence` (#1470).

## What Changes

- Archive `add-grafana-status-dashboards` → `archive/2026-10-10-add-grafana-status-dashboards/`
- Archive `feat-1367-grouped-sidebar` → `archive/2026-10-10-feat-1367-grouped-sidebar/`
- Archive `feat-1466-sonar-backend-frontend` → `archive/2026-10-10-feat-1466-sonar-backend-frontend/`
- Archive `docs-1445-owner-phase0-evidence` → `archive/2026-10-10-docs-1445-owner-phase0-evidence/`
- CHANGELOG Unreleased
- `skip_specs: true`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Active notaire-sdlc changes must reference an OPEN issue | CONSTITUTION Gate 1 | Made explicit |

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- (none)

## Impact Analysis

### Módulos afectados

| Module | Impact |
|--------|--------|
| docs/openspec | archive moves only |

## Documentation Impact

| File | Change |
|------|--------|
| archive trees above | moved |
| `CHANGELOG.md` | Unreleased |

## Out of Scope

- Owner topology decisions
