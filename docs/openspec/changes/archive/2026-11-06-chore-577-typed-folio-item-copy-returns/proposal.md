# Type Folio/Item controller returns (shrink #577 loose baseline)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `chore/577_typed-folio-item-copy-returns` |
| Gate 1 status | passed |

## Objetivo

Several write handlers still return `ResponseEntity<Object>` and sit on the loose-return ArchUnit baseline.

## Why

Issue #577: API signatures should expose DTOs/response types, not `Object`. Entity leaks are already forbidden; this tip ratchets typed returns.

## What Changes

- `ItemController#create` → `ResponseEntity<ItemResponse>`
- `FolioController#update`/`#delete` → typed returns; map Folio* domain exceptions via `FolioExceptionHandler` (mirror FolioType)
- Remove the four entries from `controller-loose-returns.txt`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Folio/Item write handlers use typed response bodies. | Issue #577; CU76 | Made explicit |

## Capabilities

### New Capabilities

- `controller-typed-returns`: Folio/Item write handlers leave the Object loose baseline.

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controllers, FolioExceptionHandler, baseline, tests |
| `frontend` | yes | — |

### Surface area

- Endpoints: same HTTP statuses; Folio conflict body shape unchanged via advice

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note (tracking issue 577) |

## Out of Scope

["Clearing the entire loose-return baseline", "Closing #577"]
