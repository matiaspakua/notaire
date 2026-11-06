# Type FolioType/DocumentType write returns (shrink #577 loose baseline)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `chore/577_typed-folio-document-type-returns` |
| Gate 1 status | passed |

## Objetivo

`FolioTypeController` and `DocumentTypeController` write handlers still declare `ResponseEntity<Object>` even though bodies are already DTOs/empty and domain exceptions are mapped by existing advice.

## Why

Issue #577: ratchets typed returns; shrinks `controller-loose-returns.txt` without wire-shape changes.

## What Changes

- `FolioTypeController#create` → `ResponseEntity<DtoFolioType>`; `#update`/`#delete` → `ResponseEntity<Void>`
- `DocumentTypeController#create` → `ResponseEntity<DtoDocumentType>`; `#update`/`#delete` → `ResponseEntity<Void>`
- Remove the six descriptors from `controller-loose-returns.txt`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| FolioType/DocumentType write handlers use typed response bodies. | Issue #577; CU76 | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

- `controller-typed-returns`: FolioType/DocumentType write handlers leave the Object loose baseline.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controllers + baseline |
| `frontend` | no | — |

### Surface area

- Endpoints: same HTTP statuses and bodies; exception handlers unchanged

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note (tracking issue 577) |

## Out of Scope

["Clearing the entire loose-return baseline", "Closing #577", "ConceptController SaveResult switches"]
