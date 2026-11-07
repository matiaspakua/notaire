# Type WorkflowValidationController#validate return (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `refactor/577_typed-workflow-validate` |
| Gate 1 status | passed |

## Objetivo

`WorkflowValidationController#validate` returns `ResponseEntity<Map<String, Object>>`, which ArchUnit treats as loosely typed because of the `Object` value type.

## Why

Issue #577 typed returns — replace with a concrete response record.

## What Changes

- Introduce `WorkflowValidationResponse(boolean valid, List<String> errors)`
- Return `ResponseEntity<WorkflowValidationResponse>`; shrink baseline by one

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Validate JSON still `{valid, errors}` | CU72 / #577 | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

- `controller-typed-returns`: WorkflowValidation validate leaves the loose baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, unit test, baseline |

### Surface area

- Same JSON keys `valid` / `errors`

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased Refs #577 |

## Out of Scope

["Closing #577"]
