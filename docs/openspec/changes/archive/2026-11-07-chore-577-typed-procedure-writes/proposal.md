# Type ProcedureController create/update via advice (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `refactor/577_typed-procedure-writes` |
| Gate 1 status | passed |

## Objetivo

`ProcedureController#create` and `#update` declare `ResponseEntity<Object>` because of mixed ProcedureResponse / empty 404 / GEH 500 bodies.

## Why

Issue #577 typed returns via `ProcedureExceptionHandler`.

## What Changes

- `ProcedureNotFoundException` + handler (empty 404); ServerError still throws to GEH
- create/update → `ResponseEntity<ProcedureResponse>`
- Shrink baseline by two

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Procedure write HTTP shapes unchanged via advice | Issue #577 | Made explicit |

## Capabilities

### Modified Capabilities

- `controller-typed-returns`: Procedure create/update leave the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, handler, domain exception, tests, baseline |

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/procedure/**`
- `backend-api/src/main/java/com/licensis/notaire/domain/procedure/ProcedureException.java`
- `backend-api/src/main/java/com/licensis/notaire/domain/procedure/ProcedureNotFoundException.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/SimpleControllersTest.java`
- `backend-api/src/test/resources/architecture/controller-loose-returns.txt`
- `docs/openspec/changes/**`
- `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md`

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased Refs #577 |
| `FLEET-LESSONS-HEX-1693.md` | P12: Write set + Risk + evidence digest on tip PRs |

## Out of Scope

["Closing #577", "Procedure delete (already Void)"]
