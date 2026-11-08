# Type DocumentCostTemplateController#createCost return (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `refactor/577_typed-document-cost-create` |
| Gate 1 status | passed |

## Objetivo

`DocumentCostTemplateController#createCost` declares `ResponseEntity<Object>` because success is `CostTemplateResponse` while invalid/not-found return `{"message":…}` maps.

## Why

Issue #577 typed returns via `DocumentCostTemplateExceptionHandler`.

## What Changes

- Domain exceptions + handler; createCost → `ResponseEntity<CostTemplateResponse>`; shrink baseline by one

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Create cost template HTTP shapes unchanged | Issue #577 | Made explicit |

## Capabilities

### Modified Capabilities

- `controller-typed-returns`: DocumentCostTemplate createCost leaves the loose baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, handler, exceptions, baseline |

### Surface area

- Same 201 / 400 / 404 JSON shapes

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased Refs #577 |

## Out of Scope

["Closing #577"]
