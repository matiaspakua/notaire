# Type ManagementStatus/WorkflowTransition deletes (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `chore/577_typed-status-workflow-deletes` |
| Gate 1 status | passed |

## Objetivo

Two delete handlers still declare `ResponseEntity<Object>` while returning only empty bodies (`204` / `404`).

## Why

Issue #577 ratchet of typed returns without wire-shape changes.

## What Changes

- `ManagementStatusController#delete` → `ResponseEntity<Void>`
- `WorkflowTransitionController#delete` → `ResponseEntity<Void>`
- Remove both from `controller-loose-returns.txt`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| These delete handlers use Void returns. | Issue #577; CU76 | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controllers + baseline |

### Surface area

- Same HTTP statuses

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note (tracking issue 577) |

## Out of Scope

["Closing #577", "Workflow deletes that still return Map conflict bodies"]
