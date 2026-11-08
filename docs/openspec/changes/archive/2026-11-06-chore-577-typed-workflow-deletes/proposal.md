# Type WorkflowDefinition/Node deletes via exception handlers (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `chore/577_typed-workflow-deletes` |
| Gate 1 status | passed |

## Objetivo

`WorkflowDefinitionController#delete` and `WorkflowNodeController#delete` still declare `ResponseEntity<Object>` because they catch NotFound/InUse in the controller and return Map bodies.

## Why

Issue #577: typed returns + thin controllers; map domain exceptions via advice (same pattern as FolioType).

## What Changes

- Add `WorkflowDefinitionExceptionHandler` and `WorkflowNodeExceptionHandler`
- Type both deletes as `ResponseEntity<Void>`; remove try/catch from delete (and NotFound catch from update)
- Shrink `controller-loose-returns.txt` by two

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Workflow definition/node delete HTTP shapes unchanged via advice | Issue #577; existing API | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controllers, handlers, tests, baseline |

### Surface area

- Same 204/404/409 `{"error":…}` shapes

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note (tracking issue 577) |

## Out of Scope

["Closing #577", "WorkflowNodeInvalidException create path"]
