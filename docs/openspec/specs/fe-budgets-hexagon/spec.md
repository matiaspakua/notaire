# fe-budgets-hexagon Specification

## Purpose
TBD - created by archiving change 2026-10-16-refactor-1827-fe-budgets-hexagon. Update Purpose after archive.

## Requirements

### Requirement: Budget UI uses core ports only

Budget list/search/mutation hooks and the budgets dashboard page SHALL call composition/use cases. The HTTP gateway SHALL be the only budgets path that imports `api-client`.

#### Scenario: useBudgets and useBudgetSearch leave the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entries for `useBudgets.ts`, `useBudgetSearch.ts`, or `useBudgets.test.tsx`

#### Scenario: Budgets dashboard leaves the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entry for `src/app/dashboard/budgets/page.tsx`

#### Scenario: REST behaviour unchanged
- **WHEN** clients create a budget with or without a procedure-type template
- **THEN** create + optional items-from-template behaviour matches the pre-slice hooks
