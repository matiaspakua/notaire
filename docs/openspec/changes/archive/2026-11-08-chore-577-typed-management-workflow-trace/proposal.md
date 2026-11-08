# Type ManagementController#getWorkflowTrace (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU83 |
| Branch | `refactor/577_typed-management-workflow-trace` |
| Gate 1 status | passed (local) |

## Objetivo

`ManagementController#getWorkflowTrace` declares `ResponseEntity<Object>`. This tip types the success path and maps expected `IllegalArgumentException` outcomes through `ManagementWorkflowTraceExceptionHandler` without changing HTTP bodies.

## What Changes

- `getWorkflowTrace` returns `ResponseEntity<DtoManagementWorkflowTrace>`.
- Expected validation failures throw `WorkflowTraceBadRequestException`; advice returns `WorkflowTraceErrorResponse` (`error` key).
- Shrink `controller-loose-returns.txt` by the `ManagementController#getWorkflowTrace` line only.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Workflow trace success stays 200 with `DtoManagementWorkflowTrace` | CU83 | Made explicit |
| Expected build failures stay 400 `{"error": …}` | existing mapping | Made explicit |

## Capabilities

### Modified Capabilities

- `controller-typed-returns`: `ManagementController#getWorkflowTrace` leaves the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Workflow trace return type, advice, unit test, baseline |

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/management/ManagementController.java`
- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/management/ManagementWorkflowTraceExceptionHandler.java`
- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/management/WorkflowTraceErrorResponse.java`
- `backend-api/src/main/java/com/licensis/notaire/domain/management/WorkflowTraceBadRequestException.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/ManagementWorkflowTraceControllerTest.java`
- `backend-api/src/test/resources/architecture/controller-loose-returns.txt`
- `CHANGELOG.md`
- `docs/openspec/**`

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased line, Refs #577 |
| `docs/openspec/specs/controller-typed-returns/spec.md` | Workflow trace requirement |

## Out of Scope

- Closing #577
- `AuditRecordController#getAll` and `AuxiliaryProtocolController#startDeed`
