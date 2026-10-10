# Pages + ADR-024 surface Phase 0 done and metrics baseline for Owner

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1445 |
| Use Case | CU76 |
| Branch | `cursor/docs-1445-owner-phase0-evidence-cf98` |
| Gate 1 status | passed |

## Objetivo

Owner-facing Pages and ADR-024 must show that Phase 0 (P0.1–P0.5) is done and link
the current metrics baseline (6/17 path-scoped) so decisions on #1445/#1438/#1226 are
evidence-backed. Agents still must not invent those choices.

## What Changes

- Architecture Pages Owner pack: “P0.1–P0.5 Done”, links to REPO-METRICS-BASELINE (6/17) and REPO-SPLIT-PLAN
- Modules Pages links to plan + metrics baseline
- ADR-024 measured evidence row records 6/17 + dorny/paths-filter
- Unit guards in `test_adr024_owner_tracker.py`
- Archive shipped `docs-1445-archive-1458-hygiene`
- `skip_specs: true`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Owner decisions cite Phase 0 evidence | #1445 / CU76 | Made explicit on Pages |

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- (none)

## Impact Analysis

### Módulos afectados

| Module | Impact |
|--------|--------|
| github-page | Architecture + Modules copy |
| docs | ADR-024 + OpenSpec |
| workspace | unit guards |

## Documentation Impact

| File | Change |
|------|--------|
| `github-page/app/docs/architecture/page.tsx` | Phase 0 + metrics |
| `github-page/app/docs/modules/page.tsx` | metrics/plan links |
| `docs/200-architecture/202-ADR/ADR-024-repository-topology.md` | 6/17 baseline |
| `CHANGELOG.md` | Unreleased |

## Out of Scope

- Inventing Owner A/B/C or LICENSE text
