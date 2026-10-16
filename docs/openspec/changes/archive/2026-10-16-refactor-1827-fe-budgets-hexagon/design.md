> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 after R1 wire DTOs #1826 and FE people #1823.

## Goals / Non-Goals

**Goals:** Shrink eslint suppressions for useBudgets, useBudgetSearch, useBudgets.test, budgets/page.
**Non-Goals:** Items catalog hexagon; payments.

## Decisions

Mirror people: domain Budget keeps wire field names (`idBudget`) to limit mapping churn; gateway owns api-client; hooks call `budgetUseCases` and map to existing `@/types` Budget for UI.

## Riesgos / Trade-offs

Budgets page used `ApiError` for 400/404 — replace with status duck-typing or gateway null-on-404 so the page needs no api-client import.

## Testing Strategy

Vitest: budget use cases, HTTP gateway, useCreateBudget; architecture-boundaries pin.

## Regression Strategy

Existing budget pagination/picker Vitest; Playwright budgets if any (n/a expected).

## Playwright Strategy

n/a — no UI behaviour change.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
