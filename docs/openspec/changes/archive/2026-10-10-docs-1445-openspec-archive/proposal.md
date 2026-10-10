# Archive shipped OpenSpec packaging changes and link business README

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1445 |
| Use Case | CU76 |
| Branch | `cursor/docs-openspec-archive-business-link-cf98` |
| Gate 1 status | passed |

## Objetivo

Ship leftover hygiene after Phase 0 packaging: archive completed OpenSpec change trees and
expose the public Business Docs URL from `docs/100-business/README.md`.

## What Changes

- Archive `docs-1441-pages-business`, `docs-1443-owner-tracker`, `docs-1445-owner-umbrella`
- Link Pages Business Docs from business README
- CHANGELOG Unreleased
- `skip_specs: true`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Completed OpenSpec packaging changes are archived after merge | CU76 / CONSTITUTION Gate 5 | Made explicit |

## Capabilities

### New Capabilities
- (none — `skip_specs: true`)

### Modified Capabilities
- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| docs | yes | OpenSpec archive + business README |

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/README.md` | Public site link |
| `CHANGELOG.md` | Unreleased |
| OpenSpec archive/ | Moved completed changes |

## Out of Scope

- Owner decisions on ADR-024 / issue 1438 / LICENSE
