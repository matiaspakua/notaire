# Count dorny/paths-filter in repository metrics baseline

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1256 |
| Use Case | CU76 |
| Branch | `cursor/chore-1256-metrics-path-filter-cf98` |
| Gate 1 status | passed |

## Objetivo

P0.1 metrics under-counted path-scoped CI after P0.2: the heuristic only saw classic
`on.paths`, so the baseline reported **2** workflows while four critical workflows use
`dorny/paths-filter`. Owner topology evidence must reflect path-scoped CI.

## What Changes

- `workspace/ci/repo-metrics.py` counts workflows with `dorny/paths-filter` or classic `paths:`
- Unit test asserts count ≥ 6 and notes mention dorny
- Regenerate `docs/300-development/REPO-METRICS-BASELINE.md`
- Clarify baseline path in `REPO-SPLIT-PLAN.md` P0.1 row
- `skip_specs: true`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Topology decisions cite reproducible metrics | #1256 / REPO-SPLIT-PLAN P0.1 | Made explicit (path-scoping coverage) |

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- (none) — metrics presentation only

## Impact Analysis

### Módulos afectados

| Module | Impact |
|--------|--------|
| workspace | metrics script + unit test |
| docs | baseline + plan row |

## Documentation Impact

| File | Change |
|------|--------|
| `docs/300-development/REPO-METRICS-BASELINE.md` | regenerated |
| `docs/300-development/REPO-SPLIT-PLAN.md` | P0.1 path/heuristic note |
| `CHANGELOG.md` | Unreleased |

## Out of Scope

- Owner ADR-024 / #1438 / LICENSE decisions
