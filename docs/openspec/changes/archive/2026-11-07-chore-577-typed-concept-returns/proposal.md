# Type ConceptController write returns via advice (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `refactor/577_concept-typed-write-returns` |
| Gate 1 status | passed |

## Objetivo

`ConceptController#createConcept`, `#updateConcept`, and `#deleteConcept` declare `ResponseEntity<Object>` because of mixed success DTO / empty 404 / `{"error"}` 409 / ErrorResponses bodies.

## Why

Issue #577 typed returns + thin controllers via `ConceptExceptionHandler`.

## What Changes

- Add domain exceptions + `ConceptExceptionHandler`
- `createConcept` → `ResponseEntity<DtoConcept>`; update/delete → `ResponseEntity<Void>`
- Throw on NotFound / InUse / Failed / unexpected SaveResult; shrink baseline by three

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Concept write HTTP shapes unchanged via advice | Issue #577 | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

- `controller-typed-returns`: Concept write methods leave the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, handler, domain exceptions, tests, baseline |

### Surface area

- Same 201/200/204/404/409/500 bodies

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note (Refs #577) |

## Out of Scope

["Closing #577"]
