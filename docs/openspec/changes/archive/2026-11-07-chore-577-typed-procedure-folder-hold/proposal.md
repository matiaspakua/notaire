# Type ProcedureFolderController#putOnHold via advice (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU85 |
| Branch | `refactor/577_typed-procedure-folder-hold` |
| Gate 1 status | passed |

## Objetivo

`ProcedureFolderController#putOnHold` declares `ResponseEntity<Object>` because of mixed DtoProcedureFolder / empty 404 / `{"message"}` 400 bodies.

## Why

Issue #577 typed returns via `ProcedureFolderExceptionHandler`.

## What Changes

- Domain exceptions + handler; method → `ResponseEntity<DtoProcedureFolder>`
- Shrink baseline by one

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| putOnHold HTTP shapes unchanged via advice | Issue #577 / CU85 | Made explicit |

## Capabilities

### Modified Capabilities

- `controller-typed-returns`: putOnHold leaves the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, handler, domain exceptions, baseline |

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/procedure/ProcedureFolderController.java`
- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/procedure/ProcedureFolderExceptionHandler.java`
- `backend-api/src/main/java/com/licensis/notaire/domain/procedure/ProcedureFolder*.java`
- `backend-api/src/test/resources/architecture/controller-loose-returns.txt`
- `docs/openspec/changes/**`

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased Refs #577 |

## Out of Scope

["Closing #577"]
