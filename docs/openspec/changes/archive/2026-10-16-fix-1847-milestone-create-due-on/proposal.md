# Milestone create omits a null due date

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1847 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/1847_milestone_create_due_on` |
| Gate 1 status | passed |

## Objetivo

The Owner's first `apply_milestone_policy.py --apply` run renamed the
milestones and synced the trains and Backlog, then stopped on the first create:
`Catalog · Requirements & use cases`, the only POST that carried
`"due_on": null`. H0, v0.1, v0.2 and Catalog were never created, so no issue moved.

## What Changes

- `create_body()` builds the POST body: state open, and no key whose value is null.
- `ensure_milestones()` uses it. PATCH (which clears a due date with null) is unchanged.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Continuous milestones have no due date | MILESTONE-NOMENCLATURE.md (#1847) | Made explicit at create time |

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none; behavior of `milestone-nomenclature` is unchanged, this fixes the tool (`skip_specs: true`).

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
| `CHANGELOG.md` | Bug Fixes entry |

## Out of Scope

- Re-running the apply (Owner, after merge).
