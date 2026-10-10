# Surface live Owner umbrella #1445 on Pages Architecture

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1445 |
| Use Case | CU76 |
| Branch | `cursor/docs-1445-pages-owner-umbrella-cf98` |
| Gate 1 status | passed |

## Objetivo

GitHub Pages `/docs/architecture/` lists ADR-024 as Proposed but does not name the
live Owner umbrella **#1445**. Sibling agents and the Owner need that tracker on the
public docs surface (same pattern as ADR-022 → #1438). Also archive the completed
OpenSpec change from the #1449 hygiene merge.

## What Changes

- Pages Architecture ADR-024 title references #1445
- Unit guard pins the Pages string
- Archive `docs-1445-archive-closed-umbrella` (shipped in #1449)
- CHANGELOG Unreleased
- `skip_specs: true`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Live Owner umbrella must remain visible on public Architecture docs until decisions are recorded | #1445, CU76 | Made explicit |

## Capabilities

### New Capabilities
- (none — docs/Pages packaging)

### Modified Capabilities
- (none)

## Impact Analysis

### Módulos afectados

| Module | Impact |
|--------|--------|
| github-page (under docs surface) | Architecture page copy |
| workspace | unit guard |
| docs/openspec | archive completed change |

## Documentation Impact

| File | Change |
|------|--------|
| `github-page/app/docs/architecture/page.tsx` | ADR-024 title → mention #1445 |
| `workspace/tests/test_adr024_owner_tracker.py` | assert Pages cites #1445 |
| `CHANGELOG.md` | Unreleased note |
| `docs/openspec/changes/archive/…` | move completed change |
| `docs/300-development/REPO-SPLIT-PLAN.md` | P0.1 note: #1256 Done but may stay OPEN (403) |

## Out of Scope

- Owner decisions themselves (ADR-024 A+B vs C, #1438 A/B/C, #1226 LICENSE)
- Closing or editing #1445 / noise stubs (#1434/#1440/#1450) — agent 403
