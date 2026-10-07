# Clear the pre-existing failures that keep the pipeline red on main

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1185 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `ci/1185_clear_pipeline_failures` |
| Gate 1 status | draft — Owner approved the plan ("proceed with your best recommendation") |

## Objetivo

`bash scripts/run_pipeline.sh` and the pre-push hook cannot pass on a clean `main`,
so every push needs `PREFLIGHT_SKIP=1` and the gate protects nothing. Three
independent causes, found while shipping #1179, are fixed here without loosening any gate.

## What Changes

- Archive the 45 OpenSpec changes whose issue is closed as COMPLETED (`openspec archive`),
  leaving only the 7 changes with an open issue. The validator is unchanged.
- Make `scripts/seed-openspec-change.sh` portable: its four `sed -i -E` calls only work with
  GNU sed, so the self-test fails on macOS (CI runners are Linux and never noticed).
- Regroup `CHANGELOG.md` `[Unreleased]` so each `###` heading appears once per release, with
  every entry preserved. Add a guard so it cannot regress.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A change whose issue is closed MUST NOT stay under `openspec/changes/` | #1185; `validate-sdlc-plan.sh`; Constitution §8 | Made explicit |
| Gate scripts MUST run on macOS and Linux | #1185; CI-PREFLIGHT | New |
| A release section MUST NOT repeat a `###` heading | #1185; `.markdownlint-cli2.jsonc` (`siblings_only`) | Made explicit |
| No gate may be disabled or loosened to get green | Constitution P8 | Made explicit |

## Capabilities

### New Capabilities

- `pipeline-green-on-main`: the pre-PR pipeline passes on a clean `main` with no bypass.

### Modified Capabilities

- (none under `openspec/specs/`)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | Removed module |
| `notaire-shared` | no | — |
| `openspec/` | yes | 45 changes moved to `changes/archive/`; delta specs folded into `openspec/specs/` |
| `scripts/` | yes | `seed-openspec-change.sh` portability; new `test_changelog_structure.py` |
| `CHANGELOG.md` | yes | Regrouped, no entry removed or reworded |

### Surface area

- Entities / Endpoints / Flyway / `.env`: none
- Dependencies: none

### Architecture review

Housekeeping and tooling. No ADR. Archiving folds delta specs into `openspec/specs/`; where
a delta cannot be applied cleanly the change is archived with `--skip-specs` and listed in
the design, never silently.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `openspec/specs/` | Receives the folded delta specs from archived changes |
| `CHANGELOG.md` | Regrouped; one entry added for this change |
| `docs/300-development/CI-PREFLIGHT.md` | Note that gate scripts must stay BSD/GNU portable |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Add #1185 to the GitHub ID table |

## Out of Scope

- The 7 changes whose issues are still open.
- Changing the validator's "issue must be OPEN" rule.
- Fixing why the CHANGELOG accumulates duplicate headings per PR (a process question).
- Closed issues #1179 and #1186 other than archiving their changes.
