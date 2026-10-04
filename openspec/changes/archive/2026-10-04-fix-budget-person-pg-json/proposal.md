# Restore nested person on Budget API responses

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1130 |
| Use Case | CU01 — Preparar Presupuesto |
| Branch | `cursor/fix-budget-person-pg-json-69d3` |
| Gate 1 status | passed |

## Objetivo

After merging [#1127](https://github.com/matiaspakua/notaire/pull/1127), main PG
Integration Tests fail on `BudgetPersonAssociationPgIntegrationTest`:
`No value at JSON path $.person.personId` (4 tests). Run:
https://github.com/matiaspakua/notaire/actions/runs/37062075513

`BudgetController.BudgetResponse` was flattened to top-level `personId` during
the #1068/#1124 DTO migration. Frontend `Presupuesto.person` / issue #883 and the
PG suite expect nested `person: { personId }` when associated, and omit `person`
when not. Bruno create/update requests already send nested person.

## What Changes

- Change `BudgetResponse` to expose nested `PersonRef person` with
  `@JsonInclude(NON_NULL)` instead of flat `personId`.
- Keep request binding accepting both nested `person` and flat `personId`.
- Add this OpenSpec change for Process Checks.

## Reglas de negocio

- CU01: a presupuesto may optionally link to a client person.
- When linked, API responses must surface the association as nested
  `person.personId` (same shape the UI sends on create/update).
- When not linked, `person` must be absent from JSON (not `null` noise).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `budget-person-response` — restore nested person on presupuesto responses.

## Impact Analysis

### Módulos afectados

| Module | Impact |
|--------|--------|
| `BudgetController` | Response DTO shape |
| Frontend `Presupuesto` | Already expects nested `person` |
| Bruno `api-test/budgets` | Request already nested; status-only asserts |
| PG IT `BudgetPersonAssociationPgIntegrationTest` | Assertions pass again |

### Risks

Low — restores prior CU01/#883 contract; #1126/#1128 rebase after merge.

## Documentation Impact

OpenSpec change is the process record. No permanent API doc rewrite required
beyond this delta (frontend types already document nested `person`).
