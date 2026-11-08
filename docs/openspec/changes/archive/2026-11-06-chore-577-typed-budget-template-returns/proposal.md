# Type BudgetTemplate write returns (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `chore/577_typed-budget-template-returns` |
| Gate 1 status | passed |

## Objetivo

`BudgetTemplateController` create/update/delete declare `ResponseEntity<?>` and sit on the loose-return baseline.

## Why

Issue #577 ratchet; bodies are already a Map / empty — type them without wire-shape change.

## What Changes

- create → `ResponseEntity<Map<String, Integer>>`
- update/delete → `ResponseEntity<Void>`
- Remove three baseline entries

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| BudgetTemplate write handlers use typed returns | Issue #577; CU76 | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller + baseline |

### Surface area

- Same HTTP statuses and JSON keys

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note (tracking issue 577) |

## Out of Scope

["Closing #577"]
