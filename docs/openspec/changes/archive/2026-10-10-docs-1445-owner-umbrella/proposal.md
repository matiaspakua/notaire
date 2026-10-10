# Point topology docs at live Owner umbrella #1445

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1445 |
| Use Case | CU76 |
| Branch | `cursor/docs-owner-umbrella-tracker-cf98` |
| Gate 1 status | passed |

## Objetivo

Prior Owner umbrellas #1197 and #1443 were closed by GitHub keyword parsing. Live tracker is #1445.

## What Changes

- ADR-024 / REPO-SPLIT-PLAN → #1445
- Update unit guard
- CHANGELOG Unreleased
- `skip_specs: true`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Live Owner umbrella must remain open until decisions are recorded | #1445, CU76 | Made explicit |

## Capabilities

### New Capabilities
- (none — )

### Modified Capabilities
- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| docs | yes | ADR-024, REPO-SPLIT-PLAN, OpenSpec |
| workspace | yes | unit guard |

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| ADR-024 | Deciders + tracker note → #1445 |
| REPO-SPLIT-PLAN.md | Issue map umbrella → #1445 |
| CHANGELOG.md | Unreleased |

## Out of Scope

- Recording Owner decisions
- Reopening closed umbrellas
