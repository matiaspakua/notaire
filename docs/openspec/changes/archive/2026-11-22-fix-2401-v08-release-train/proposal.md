# fix-2401-v08-release-train — Add the v0.8 release train

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2401 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/2401_v08_release_train` |
| Gate 1 status | passed |

## Objetivo

Release 0.8.0 shipped without a corresponding release train entry, so every
`train_for("0.8.0")` call failed with "no milestone titled v0.8…". The
decision is to add the train `v0.8 · Observable self-managing factory (2027-03)`
in all three canonical sources so that product version coherence passes.

## What Changes

- `RELEASE_TRAINS` in `workspace/release/product_release.py` gains the v0.8 entry.
- `OBSERVABLE_FACTORY` constant, `SCHEDULED` list, `MILESTONE_WINDOW`, and
  `MILESTONE_DESCRIPTION` in `workspace/sdlc/milestone_policy.py` gain the v0.8 entries.
- The current-map table in `docs/300-development/305-project-management/MILESTONE-NOMENCLATURE.md`
  gains the v0.8 row.
- `test_milestone_policy.py` hardcoded SCHEDULED assertion updated to include v0.8.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every product minor must have a release train | CU76 — QA & Testing Infrastructure | Made explicit |

## Capabilities

### New Capabilities

*(none)*

### Modified Capabilities

- `product-release`: adds the requirement that every product minor has a release train

## Impact Analysis

### Módulos afectados

- `workspace/release/product_release.py` — RELEASE_TRAINS constant
- `workspace/sdlc/milestone_policy.py` — OBSERVABLE_FACTORY, SCHEDULED, windows, descriptions
- `workspace/sdlc/test_milestone_policy.py` — SCHEDULED assertion
- `docs/300-development/305-project-management/MILESTONE-NOMENCLATURE.md` — v0.8 row

## Documentation Impact

| Document | Change |
|----------|--------|
| `workspace/release/product_release.py` | v0.8 train entry added |
| `workspace/sdlc/milestone_policy.py` | OBSERVABLE_FACTORY + SCHEDULED |
| `workspace/sdlc/test_milestone_policy.py` | assertion extended |
| `docs/300-development/305-project-management/MILESTONE-NOMENCLATURE.md` | v0.8 row |
| `docs/openspec/changes/fix-2401-v08-release-train/**` | this change set |
| `docs/openspec/changes/archive/**fix-2401*/**` | archived after PR |
| `docs/openspec/specs/**` | delta added to product-release spec |

### Write set

- `workspace/release/product_release.py`
- `workspace/sdlc/milestone_policy.py`
- `workspace/sdlc/test_milestone_policy.py`
- `docs/300-development/305-project-management/MILESTONE-NOMENCLATURE.md`
- `docs/openspec/changes/fix-2401-v08-release-train/**`
- `docs/openspec/changes/archive/**fix-2401*/**`
- `docs/openspec/specs/**`
- `github-page/lib/releases.json`

### Breaking Changes

None — the fix restores previously expected behaviour.

### Rollback

Revert commit 55d876f9. Safe: no DB migration, no API surface change.
