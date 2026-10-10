# path-scoped-ci

Pull requests that change only documentation or agent guidance skip irrelevant heavy CI jobs
while required check names stay green.

## ADDED Requirements

### Requirement: Path filter job on heavy workflows

The workflows `ci.yml`, `frontend-ci.yml`, `playwright-e2e.yml`, and `openapi-contract.yml`
MUST include a `changes` job named `Path filter` that classifies PR paths into at least
`backend`, `frontend`, `testing`, `docs`, `ci`, `openapi`, and `product` outputs.

#### Scenario: Changes job present

- **WHEN** the four workflows are inspected
- **THEN** each defines `jobs.changes` with display name `Path filter`

### Requirement: Docs-only PRs skip Java and Playwright leaves

When a pull request changes only documentation / agent paths (docs filter true and backend,
frontend, testing, and ci false), Java Unit/Integration/Coverage leaf jobs and Playwright/Bruno
leaf jobs MUST be skipped, and suite aggregators MUST still conclude successfully.

#### Scenario: Docs-only PR

- **WHEN** a PR changes only `docs/**` (and optionally `CONSTITUTION.md` / `AGENTS.md`)
- **THEN** `build` in `ci.yml` and `backend-build` / `frontend-build` in `playwright-e2e.yml` are skipped
- **AND** suite aggregators `CI`, `Frontend CI`, and `Playwright E2E` conclude successfully

### Requirement: Product changes still run product suites

When a pull request changes product paths (`backend`, `frontend`, `testing`, or `ci`), the
corresponding heavy suites MUST still run so regressions are not silently skipped.

#### Scenario: Backend PR

- **WHEN** a PR changes `backend-api/**`
- **THEN** `ci.yml` build runs and Playwright `product` path runs

### Requirement: Main always full suite

Non-pull-request events MUST NOT skip leaf jobs for path reasons. The path filter MUST force
all outputs true (or equivalent) so `main` push, dispatch, and schedule run the full suite.

#### Scenario: Push to main

- **WHEN** the event is `push` to `main` (or non-PR dispatch/schedule)
- **THEN** path filter outputs are all effectively true and leaf jobs are not skipped for path reasons

### Requirement: Aggregators accept intentional skips

Suite aggregator jobs that publish required check names MUST treat a needed job result of
`skipped` as success when that skip is caused by path filters, while still failing on
`failure` or `cancelled`.

#### Scenario: Aggregator script

- **WHEN** a suite aggregator evaluates a needed job result of `skipped`
- **THEN** it does not fail solely for that reason (`success|skipped` accepted)

### Requirement: No workflow-level on.paths on heavy suites

The four heavy workflows MUST NOT use workflow-level `on.paths` filters on `pull_request` or
`push` that would omit the entire workflow (and its required checks) from running on `main`.

#### Scenario: Trigger config

- **WHEN** `pull_request` / `push` triggers are inspected on the four workflows
- **THEN** they do not use `on.paths` filters that would omit the workflow entirely on main
