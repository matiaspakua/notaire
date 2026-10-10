# Archive OpenSpec trees tied to closed umbrella #1197

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1445 |
| Use Case | CU76 |
| Branch | `cursor/chore-openspec-archive-closed-1197-cf98` |
| Gate 1 status | passed |

## Objetivo

`workspace/verify` failed because active change `docs-1197-repository-topology` still
required OPEN issue #1197, which was closed by keyword parsing. Archive that tree (and the
already-merged docs-1445-openspec-archive packaging change) so SDLC plan validation is green.

## What Changes

- Archive `docs-1197-repository-topology` and `docs-1445-openspec-archive`
- CHANGELOG Unreleased
- `skip_specs: true`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Active notaire-sdlc changes must reference an OPEN issue | CONSTITUTION Gate 1 / validate-sdlc-plan.sh | Made explicit |

## Capabilities

### New Capabilities
- (none — `skip_specs: true`)

### Modified Capabilities
- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| docs | yes | OpenSpec archive |
| workspace | yes | verify becomes green |

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased |
| OpenSpec archive/ | Moved trees |

## Out of Scope

- Owner decisions on ADR-024 / issue 1438 / LICENSE
