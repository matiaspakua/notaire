# Retired milestones are deleted, stale views are reported

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1847 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/1847_retired_milestones_stale_views` |
| Gate 1 s## Objetivo

After the Owner runs, the two History buckets merged into H8 (#13, #14) stay as
empty closed milestones with their old titles, and the pre-taxonomy views
`Casos de Uso` and `RNF` stay on the board. `--check` reported neither.

## What Changes

- `retired_milestones()` / `retire_milestones()`: an empty rename source whose target exists is deleted by `--apply` and listed by `--dry-run`.
- `stale_views()`: `--check` counts views outside `VIEW_CONTRACTS` (old names still to be renamed excepted) and retired milestones.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A merged-away milestone does not remain | MILESTONE-NOMENCLATURE.md, Owner consolidation request | New |

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `milestone-nomenclature` (ADDED requirement).

p_specs: true`).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github/workflows`) | no | – |
| `workspace` | yes | `apply_milestone_policy.py`, its test |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Tool fix inside the existing policy/apply split; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |

## Out of Scope

- Re-running the apply (Owner, after merge).
