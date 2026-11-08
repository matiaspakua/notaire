## ADDED Requirements

### Requirement: Required fields surface FormField errors before submit

High-traffic create/edit dialogs for budgets, deeds, and managements SHALL block
save when required values are empty and SHALL show the failure on the matching
`FormField` via its `error` prop (same pattern people already use).

#### Scenario: Empty required deed fields block save

- **WHEN** the user opens New deed and clicks Create without number, date, or folio
- **THEN** the mutation is not called and each empty required field shows an error

#### Scenario: Empty required budget client blocks save

- **WHEN** the user opens New quote and clicks Create without a client
- **THEN** the mutation is not called and the client field shows an error

### Requirement: List query failures invite retry on budgets and deeds

Budgets and deeds list pages SHALL pass a translated load error into DataTable
`error` with `onRetry` when the primary page query fails, matching people and
managements (#1947).

#### Scenario: Budgets list query fails

- **WHEN** the budgets page query is in error
- **THEN** DataTable shows the failure invitation and a retry control
