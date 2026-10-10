# Archive shipped OpenSpec Gate 1 trees after #1457 / #1458

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1445 |
| Use Case | CU76 |
| Branch | `cursor/chore-openspec-archive-1256-1457-cf98` |
| Gate 1 status | passed |

## Objetivo

Archive shipped Gate 1 trees so the active OpenSpec set stays tidy after
#1457 and #1458 merged.

## What Changes

- Archive `docs-1445-archive-1456-gate1` → `archive/2026-10-10-docs-1445-archive-1456-gate1/`
- Archive `docs-1256-metrics-path-filter` → `archive/2026-10-10-docs-1256-metrics-path-filter/`
- CHANGELOG Unreleased
- `skip_specs: true`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Shipped Gate 1 changes leave the active set | CONSTITUTION / OpenSpec hygiene | Made explicit |

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
