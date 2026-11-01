# Rename Cleanup Actions workflow file (leading-c typo)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1981 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `chore/1981_rename-cleanup-actions-workflow` |
| Gate 1 status | passed |

## Objetivo

The scheduled Actions cleanup workflow file dropped the leading `c` in its filename,
so search and the Pages stack inventory looked wrong. Rename it and update permanent
references so inventories and docs match the real path.

## Why

Filename typo blocks discoverability and makes the Pages `/docs/stack/` list look wrong.

## What Changes

- `git mv` the workflow to `cleanup-actions.yml`
- Update permanent docs / metrics / Pages inventory references
- Add a Process Checks ratchet so the typo filename cannot return

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Cleanup Actions workflow file is named cleanup-actions.yml | #1981 | Made explicit |

## Capabilities

### New Capabilities

- `cleanup-actions-workflow-name`: typo filename is gone and guarded by a unit test

### Modified Capabilities

(none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `.github/workflows` | yes | rename |
| `workspace` | yes | metrics text + ratchet test |
| `github-page` | yes | inventory + README |

### Surface area

Schedule and workflow_dispatch behavior unchanged; filename only.

### Architecture review

Ops hygiene; no product API change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `github-page/README.md` | filename |
| Pages stack inventory | filename |
| CHANGELOG | Unreleased note |

## Out of Scope

Changing the cleanup retention policy itself.
