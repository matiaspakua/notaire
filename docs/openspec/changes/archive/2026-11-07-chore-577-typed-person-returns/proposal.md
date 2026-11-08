# Type PersonController write returns via advice (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `refactor/577_typed-person-returns` |
| Gate 1 status | passed |

## Objetivo

`PersonController#createPerson`, `#updatePerson`, and `#deletePerson` declare `ResponseEntity<Object>` because of mixed PersonResponse / duplicate Map / ErrorResponses / plain-text conflict bodies.

## Why

Issue #577 typed returns + thin controllers via `PersonExceptionHandler`.

## What Changes

- Domain exceptions + `PersonExceptionHandler` (duplicate conflict body as typed record)
- create/update → `ResponseEntity<PersonResponse>`; delete → `ResponseEntity<Void>`
- Shrink baseline by three

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Person write HTTP shapes unchanged via advice | Issue #577 | Made explicit |

## Capabilities

### Modified Capabilities

- `controller-typed-returns`: Person write methods leave the loose-return baseline

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

["Closing #577"]
