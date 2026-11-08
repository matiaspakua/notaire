# Type ProcedureTypeController write returns via advice (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `refactor/577_typed-procedure-type-writes` |
| Gate 1 status | passed |

## Objetivo

`ProcedureTypeController#create`, `#update`, and `#delete` declare `ResponseEntity<Object>` because of mixed DTO / empty / `{"error"}` / ErrorResponses bodies.

## Why

Issue #577 typed returns + thin controllers via `ProcedureTypeExceptionHandler`.

## What Changes

- Domain exceptions + handler; create → `DtoProcedureType`; update/delete → `Void`; shrink baseline by three
- `assignWorkflow` stays for a follow-on tip (Map body + Object return)

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Procedure type write HTTP shapes unchanged via advice | Issue #577 | Made explicit |

## Capabilities

### Modified Capabilities

- `controller-typed-returns`: ProcedureType create/update/delete leave the loose baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, handler, exceptions, tests, baseline |

### Surface area

- Same 201/200/204/404/409/500 bodies

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased Refs #577 |

## Out of Scope

["Closing #577", "Typing assignWorkflow"]
