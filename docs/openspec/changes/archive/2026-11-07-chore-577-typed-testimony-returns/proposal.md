# Type TestimonyController write returns via advice (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `refactor/577_typed-testimony-returns` |
| Gate 1 status | passed |

## Objetivo

`TestimonyController#create`, `#update`, and `#delete` declare `ResponseEntity<Object>` because of mixed DtoTestimony / ErrorResponses / plain-text conflict / empty 404 bodies.

## Why

Issue #577 typed returns + thin controllers via `TestimonyExceptionHandler`.

## What Changes

- Domain exceptions + `TestimonyExceptionHandler` (createFailed/updateFailed/staleVersion/plain-text 409)
- create → `ResponseEntity<DtoTestimony>`; update/delete → `ResponseEntity<Void>`
- Shrink baseline by three

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Testimony write HTTP shapes unchanged via advice | Issue #577 | Made explicit |

## Capabilities

### Modified Capabilities

- `controller-typed-returns`: Testimony write methods leave the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, handler, domain exceptions, tests, baseline |

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased Refs #577 |

## Out of Scope

["Closing #577", "TestimonyMovementController (follow-up tip)"]
