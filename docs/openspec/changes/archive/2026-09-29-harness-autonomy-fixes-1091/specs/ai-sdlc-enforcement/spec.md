## ADDED Requirements

### Requirement: The harness repairs tasks.md to base plus ticks

After Gate 2, when the worker changes `tasks.md` beyond ticking boxes, the
harness SHALL restore the base file and SHALL keep only the `[x]` ticks the
worker set on base items, matched by task ID.

#### Scenario: Tick kept

- **WHEN** the worker's file ticks item `8.1`, which is unticked in the base
- **THEN** the repaired file ticks `8.1`

#### Scenario: Added item dropped

- **WHEN** the worker's file adds an item `5.4` absent from the base
- **THEN** the repaired file equals the base apart from ticks

#### Scenario: Not-applicable mark dropped

- **WHEN** the worker's file marks base item `8.2` as `[n/a]`
- **THEN** the repaired file keeps `8.2` unticked with its base text

### Requirement: The spec gate requires the ledger rows

The spec gate SHALL reject a `traceability.md` in which the `Commits` or the
`Pull Request` row is not present exactly once.

#### Scenario: Ledger rows present

- **WHEN** `traceability.md` has one `Commits` row and one `Pull Request` row
- **THEN** the row check passes

#### Scenario: Ledger row missing

- **WHEN** `traceability.md` has no `Commits` row
- **THEN** the row check fails and names `Commits`

### Requirement: The adapter provides a Markdown lint fix command

The project adapter SHALL declare `gates.docs_lint_fix`, which the harness runs
on the Markdown files it lints before it lints them.

#### Scenario: Adapter declares the lint fix command

- **WHEN** the adapter lacks `gates.docs_lint_fix`
- **THEN** adapter validation reports it missing
