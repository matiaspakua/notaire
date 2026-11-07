# Type ProcedureTypeController#assignWorkflow via advice (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `refactor/577_typed-assign-workflow` |
| Gate 1 status | passed |

## Objetivo

`ProcedureTypeController#assignWorkflow` declares `ResponseEntity<Object>` because of mixed DtoProcedureType / empty 404 / `{"error"}` 409 bodies.

## Why

Issue #577 typed returns via existing `ProcedureTypeExceptionHandler`.

## What Changes

- NotFound → `ProcedureTypeNotFoundException`; Inactive → `ProcedureTypeInUseException` (same `{"error"}` 409 shape)
- Method → `ResponseEntity<DtoProcedureType>`
- Shrink baseline by one

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| assignWorkflow HTTP shapes unchanged via advice | Issue #577 | Made explicit |

## Capabilities

### Modified Capabilities

- `controller-typed-returns`: assignWorkflow leaves the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, baseline, OpenSpec |

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased Refs #577 |

## Out of Scope

["Closing #577"]
