# Delete stale Jenkinsfile

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1049 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1049_delete_stale_jenkinsfile` |
| Gate 1 status | pending |

## Objetivo

The legacy Jenkinsfile is no longer referenced by any CI/CD workflow and conflicts with the new GitHub Actions based pipeline.
Its removal simplifies the repo layout and eliminates outdated build artefacts.

## What Changes

- Delete the legacy `Jenkinsfile` from the repository.

## Reglas de negocio

None. No code behavior changes.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — no spec-level (application) behavior changes; `skip_specs: true` is set in `.openspec.yaml`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| CI/CD (`.github/workflows`, `scripts`) | yes | Delete the obsolete `Jenkinsfile` and add a documentation notice. |

## Surface area

- Files: `Jenkinsfile`
- Build scripts: none changed except removal.
- No runtime impact.

## Documentation Impact

The repo’s documentation is updated to reflect the removal of Jenkinsfile and the shift to GitHub Actions.
