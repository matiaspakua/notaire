# Pages Architecture lists remaining Owner decisions

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1445 |
| Use Case | CU76 |
| Branch | `cursor/docs-1445-pages-owner-decisions-cf98` |
| Gate 1 status | passed |

## Objetivo

Sibling agents and the Owner need a single public surface that names every
blocking Owner decision for topology prep: umbrella #1445 (ADR-024), P0.6 #1438
(ADR-022 A/B/C), and LICENSE #1226. ADR titles alone do not list #1226.

## What Changes

- Architecture Pages section “Owner decisions pending” with issue links
- Unit guard pins #1445, #1438, #1226 on the Architecture page
- Archive completed `docs-1445-archive-hygiene-1452`
- CHANGELOG Unreleased
- `skip_specs: true`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Remaining Owner topology decisions must stay discoverable until recorded | #1445, CU76 | Made explicit |

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- (none)

## Impact Analysis

### Módulos afectados

| Module | Impact |
|--------|--------|
| github-page | Architecture page copy |
| workspace | unit guard |
| docs/openspec | archive completed change |

## Documentation Impact

| File | Change |
|------|--------|
| `github-page/app/docs/architecture/page.tsx` | Owner decisions pending section |
| `workspace/tests/test_adr024_owner_tracker.py` | assert #1445/#1438/#1226 |
| `CHANGELOG.md` | Unreleased |
| `docs/openspec/changes/archive/…` | archive hygiene-1452 |

## Out of Scope

- Recording Owner choices (must not invent LICENSE / A+B/C / ADR-024 accept)
- Closing noise stubs (#1434/#1440/#1450) — agent 403
