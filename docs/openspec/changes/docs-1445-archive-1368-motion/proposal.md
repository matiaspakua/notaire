# Archive closed-issue OpenSpec fix-1368-motion-tokens

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1445 |
| Use Case | CU76 |
| Branch | `cursor/chore-openspec-archive-1368-cf98` |
| Gate 1 status | passed |

## Objetivo

After #1433 merged, active OpenSpec `fix-1368-motion-tokens` still required OPEN
#1368 (now CLOSED). Archive it so `validate-sdlc-plan.sh` stays green.

## What Changes

- Archive `fix-1368-motion-tokens` → `archive/2026-10-10-fix-1368-motion-tokens/`
- CHANGELOG Unreleased
- `skip_specs: true`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Active notaire-sdlc changes must reference an OPEN issue | CONSTITUTION Gate 1 / validate-sdlc-plan.sh | Made explicit |

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- (none)

## Impact Analysis

### Módulos afectados

| Module | Impact |
|--------|--------|
| docs/openspec | archive move only |

## Documentation Impact

| File | Change |
|------|--------|
| `docs/openspec/changes/archive/2026-10-10-fix-1368-motion-tokens/` | archived tree |
| `CHANGELOG.md` | Unreleased note |

## Out of Scope

- Owner topology decisions
