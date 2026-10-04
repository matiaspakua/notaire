# Design — nested person on BudgetResponse

## Context

\#1124/#1127 DTO migration flattened `BudgetResponse` to `personId`. Frontend
and PG ITs (#883 / CU01) still require nested `person`. Main is red on
`e8f665ed` PG job.

## Goals / Non-Goals

### Goals

- Restore nested `person` on responses with NON_NULL inclusion.
- Unblock main + sibling PRs (#1126/#1128) after rebase.

### Non-Goals

- Changing request binding (already accepts nested + flat).
- Expanding person payload beyond `{ personId }`.
- UI changes (UI already nested).

## Decisions

1. **Nested `PersonRef` on `BudgetResponse`** — match frontend `Presupuesto.person`
   and Bruno request shape.
2. **`@JsonInclude(NON_NULL)`** — omit `person` when unassociated so
   `jsonPath("$.person").doesNotExist()` holds.
3. **Drop flat response `personId`** — dual fields would confuse; request still
   accepts flat `personId` for compatibility.

## Riesgos

| Risk | Mitigation |
|------|------------|
| Client depended on flat `personId` | Frontend uses nested; Bruno only checks status/idBudget |
| Sibling PR conflicts | Prefer this PR merge first; others rebase |

## Testing Strategy

- Existing `BudgetPersonAssociationPgIntegrationTest` (4 failing assertions) =
  Acceptance Criteria; prove red on main, green after fix.
- H2 suites that create budgets with nested person continue to pass.

## Regression Strategy

- Unit + Integration + Coverage Gate on PR CI.
- Bruno budgets folder (create/update use nested person).

## Playwright Strategy

n/a — no UI change; presupuestos page already consumes nested `person`.

## Deployment Strategy

Standard merge to main; no migration; no config.

## Rollback Strategy

Revert the `BudgetResponse` commit; main returns to flat `personId` and PG IT
red again — prefer forward fix.
