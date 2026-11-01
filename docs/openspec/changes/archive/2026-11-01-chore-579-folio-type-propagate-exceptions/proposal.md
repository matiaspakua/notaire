# Folio-type controller lets unexpected errors reach GlobalExceptionHandler

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #579 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `chore/579_folio-type-propagate-exceptions` |
| Gate 1 status | passed |

## Objetivo

`FolioTypeController` create/update wrap persistence failures in `ErrorResponses.createFailed` /
`updateFailed`, which maps unexpected `RuntimeException` to HTTP **409 Conflict** instead of the
standard 500 `ErrorResponse` from `GlobalExceptionHandler`. Domain `FolioTypeException` already
passes through. Remove the catch-alls so unexpected failures use GEH; keep domain rethrows.

## What Changes

- Delete `catch (Exception)` from create/update (keep `FolioTypeException` rethrow until advice covers it)
- Add `FolioTypeControllerTest` scenario: unexpected create failure → 500 `ErrorResponse`
- Document as #579 folio-type slice (umbrella stays open)

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Unexpected folio-type API failures return the standard ErrorResponse via GlobalExceptionHandler | #579, CU76 | Made explicit for folio types |
| Unexpected create failures must not be mapped to 409 Conflict | #579 | Changed (was ErrorResponses.conflict) |

## Capabilities

### New Capabilities

- `folio-type-exception-propagation`: folio-type adapter does not swallow unexpected exceptions as 409

### Modified Capabilities

(none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | FolioTypeController + unit test |
| docs | yes | OpenSpec |

### Surface area

Unexpected create failures change from 409 (ErrorResponses) to 500 ErrorResponse. Constraint /
unique violations still map via GlobalExceptionHandler (400/409). Domain FolioTypeException paths
unchanged.

### Architecture review

Advances #579 toward GlobalExceptionHandler as the single unexpected-error path.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Omit Unreleased while #1990 holds the fence; note on Issue |

## Out of Scope

Other controllers; removing FolioTypeExceptionHandler; changing domain 409 message bodies.
