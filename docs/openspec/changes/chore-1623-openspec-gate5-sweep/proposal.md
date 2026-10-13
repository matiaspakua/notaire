# Archive the 32 shipped OpenSpec changes whose issues are closed (Gate 5 sweep, slice 1)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1623 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1623_archive-shipped-openspec` |
| Gate 1 status | passed |

## Objetivo

32 active OpenSpec changes point at **closed** issues. `validate-sdlc-plan.sh`
checks every active change, so `PR Validation / SDLC Plan Validation` is red on
**every** PR in the repository. `check-openspec-archive-hygiene.py
--inventory-only` lists the same 32. This slice archives them so the gate can mean
something again.

## What Changes

- `openspec archive --yes` for each of the 32 changes. Their delta specs merge into
  `docs/openspec/specs/` (13 new capabilities, several updated).
- 22 of the deltas could not archive as written. 21 used `## MODIFIED
  Requirements` for capabilities or requirements that did not exist yet, and one
  (`fix-1599`) used `## ADDED` for a requirement that #1591 had already added.
  Each header was corrected to match the main spec. No requirement or scenario
  text was changed.
- 12 main specs had a `Purpose` under 50 characters, so `openspec validate
  --specs --strict` warned on them (9 created by this sweep, 3 already on `main`).
  Each Purpose now states the capability and its source issue. Result: 168/168
  specs pass (165 on `main`).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A shipped change is archived and its deltas live in the main specs | CONSTITUTION Gate 5, #1252 | Unchanged — applied |
| A delta's operation matches the main spec (`ADDED` new, `MODIFIED` existing) | OpenSpec schema | Made explicit (enforcement is slice 2 of #1623) |

## Capabilities

### New Capabilities

### Modified Capabilities

None of its own (`skip_specs: true`). The archived changes bring their reviewed deltas.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `docs/openspec` | yes | 32 changes moved to `changes/archive/2026-10-13-*`; specs synced |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/openspec/specs/**` | Synced from the archived deltas (that is the change) |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

Tracked on #1623 as later slices:

- ~28 active changes that are merged slices of still-open umbrella issues (#655, #579, #596, #1315, #595, #1250, #1242, #921).
- Validator: reject `MODIFIED` and `REMOVED` deltas whose target is missing, and `ADDED` deltas whose target already exists, at Gate 1.
- Hygiene: flag an active change when its PR has merged, not only when its issue is closed.
- Owner and timing of archiving in the foreman playbooks.
