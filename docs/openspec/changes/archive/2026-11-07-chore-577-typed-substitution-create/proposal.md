# Type SubstitutionController#create return (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `refactor/577_typed-substitution-create` |
| Gate 1 status | passed |

## Objetivo

`SubstitutionController#create` declares `ResponseEntity<Object>` because success is `SubstitutionResponse` while Conflict returns `{"error":…}` and NotFound is empty 404.

## Why

Issue #577 typed returns via `SubstitutionExceptionHandler`.

## What Changes

- Domain exceptions + handler; create → `ResponseEntity<SubstitutionResponse>`; shrink baseline by one

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Substitution create HTTP shapes unchanged | Issue #577 / #579 | Made explicit |

## Capabilities

### Modified Capabilities

- `controller-typed-returns`: Substitution create leaves the loose baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, handler, exceptions, baseline, unit advice |

### Surface area

- Same 201 / 404 / 409 / 500 bodies

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased Refs #577 |

## Out of Scope

["Closing #577"]
