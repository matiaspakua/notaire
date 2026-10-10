# Archive OpenSpec trees after #1477 / #1480

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1445 |
| Use Case | CU76 |
| Branch | `cursor/chore-openspec-archive-1462-cf98` |
| Gate 1 status | passed |

## Objetivo

`validate-sdlc-plan.sh` fails on main: active OpenSpec `feat-1462-otel-metrics-exporters`
still requires OPEN #1462 (CLOSED after #1477). Also archive shipped Gate 1 trees from
#1473 / #1480 so active changes stay lean.

## What Changes

- Archive `feat-1462-otel-metrics-exporters` → `archive/2026-10-10-feat-1462-otel-metrics-exporters/`
- Archive `docs-1445-archive-closed-1472` → `archive/2026-10-10-docs-1445-archive-closed-1472/`
- Archive `docs-1479-modules-owner-tracker` → `archive/2026-10-10-docs-1479-modules-owner-tracker/`
- CHANGELOG Unreleased
- `skip_specs: true`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Active OpenSpecs cite OPEN issues | CONSTITUTION / validate-sdlc-plan | Enforced |

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- (none)

## Impact Analysis

### Módulos afectados

| Module | Impact |
|--------|--------|
| docs | OpenSpec archive moves |

## Documentation Impact

| File | Change |
|------|--------|
| `docs/openspec/changes/` | archive three trees; add Gate 1 |
| `CHANGELOG.md` | Unreleased |

## Out of Scope

- Owner ADR-024 / #1438 / #1226 decisions; inventing LICENSE
