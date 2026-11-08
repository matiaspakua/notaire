# Type TestimonyMovementController write returns via advice (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `refactor/577_typed-testimony-movement-returns` |
| Gate 1 status | passed |

## Objetivo

`TestimonyMovementController#create`, `#update`, and `#delete` declare `ResponseEntity<Object>` because of mixed DTO / ErrorResponses / plain-text conflict / empty bodies.

## Why

Issue #577 typed returns + thin controllers via `TestimonyMovementExceptionHandler`.

## What Changes

- `TestimonyMovementExceptionHandler` (reuses testimony domain exceptions; same HTTP shapes)
- create → `ResponseEntity<DtoTestimonyMovement>`; update/delete → `ResponseEntity<Void>`
- Shrink baseline by three

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| TestimonyMovement write HTTP shapes unchanged via advice | Issue #577 | Made explicit |

## Capabilities

### Modified Capabilities

- `controller-typed-returns`: TestimonyMovement write methods leave the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, handler, tests, baseline |

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased Refs #577 |

## Out of Scope

["Closing #577"]
