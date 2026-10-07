# Organize scripts/ into their modules

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1307 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `refactor/1307_scripts_stack` (slice 1), later slices on their own branches |
| Gate 1 status | approved by the Owner on 2026-10-07 (layout, `workspace/` as home) |

## Objetivo

`scripts/` holds 45 files for five unrelated purposes. Following ADR-026 each script belongs to the module it serves; the SDLC and run-the-system scripts belong to `workspace/`, the unifier. Phase 1 of #1292 made modules; this change empties `scripts/`.

## What Changes

- Slice 1: `start`, `stop`, `logs`, `start-all`, `setup-pgadmin` move to `workspace/stack/`; they locate the repo root two levels up (`stop`, `logs` and `setup-pgadmin` used their own folder as the root and only worked from it).
- Slice 2: module tools to `backend-api/tools/`, `docs/tools/`, `testing/tools/`, `security/`.
- Slice 3: SDLC gates and CI report generators to `workspace/sdlc/` and `workspace/ci/`, with the pre-push hook, workflows and agents.
- Slice 4: guards to their module, deletion of `validate-cu-api-matrix.sh` (no callers) and of the 25 wrappers in `scripts/tests/`, removal of `scripts/`.
- `workspace/tests/test_scripts_layout.py`: every moved script exists at its new path, is gone from the old one, and no tracked file references an old path.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A script lives in the module it serves; cross-module SDLC tooling lives in `workspace/` | ADR-026 | Made explicit |

## Capabilities

### New Capabilities

- `scripts-layout`: scripts are organized by module with no stale references.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `workspace` | yes | receives stack, SDLC and CI scripts |
| `backend-api`, `docs`, `testing`, `security`, `infra` | yes (later slices) | receive their tools and guards |
| `.github/workflows`, `.githooks`, `.claude`, docs | yes | paths updated |

### Surface area

- Endpoints, entities, Flyway, dependencies: none
- Risk: a stale path silently disables a gate or breaks `start.sh`/`preflight.sh`. Mitigation: the layout guard fails on any stale reference; each slice runs `preflight`; `--full` after the last.

### Architecture review

No new decision; applies ADR-026.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `AGENTS.md`, `README.md`, setup and deployment docs | script paths |
| `workspace/MODULE.md` | new subfolders |
| `CHANGELOG.md` | one entry per slice |
