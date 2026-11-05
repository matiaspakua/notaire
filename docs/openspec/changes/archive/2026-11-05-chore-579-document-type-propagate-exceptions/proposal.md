# DocumentType controller lets unexpected errors reach GlobalExceptionHandler

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #579 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `chore/579_document-type-propagate-exceptions` |
| Gate 1 status | passed |

## Objetivo

`DocumentTypeController.create` wraps persistence failures in `ErrorResponses.createFailed`,
which maps unexpected `RuntimeException` to HTTP **409 Conflict** instead of the
standard 500 `ErrorResponse` from `GlobalExceptionHandler`. Remove the catch-all.

## What Changes

- Delete `catch (Exception)` from create
- Add `DocumentTypeControllerTest` scenario: unexpected create failure → 500 `ErrorResponse`
- Document as #579 document-type slice (umbrella stays open)

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Unexpected document-type API failures return the standard ErrorResponse via GlobalExceptionHandler | #579, CU76 | Made explicit for document-types |
| Unexpected create failures must not be mapped to 409 Conflict | #579 | Changed |

## Capabilities

### New Capabilities

- `document-type-exception-propagation`: document-type adapter does not swallow unexpected exceptions as 409

### Modified Capabilities

(none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | DocumentTypeController + unit test |
| docs | yes | OpenSpec |

### Surface area

Unexpected create failures change from 409 to 500 ErrorResponse. Constraint paths
still map via GlobalExceptionHandler.

### Architecture review

Advances #579 toward GlobalExceptionHandler as the single unexpected-error path.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note (after Unreleased fence frees) |

## Out of Scope

Other controllers; PUT/DELETE paths already without catch-all.
