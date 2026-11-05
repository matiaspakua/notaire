# release-only-github-actions Specification

## Purpose
Product GitHub Actions for build, test, contract, database V&V, process, and coverage run when a version is released, not on every pull request. Day-to-day proof is the local full preflight.

## Requirements

### Requirement: Pull requests are gated locally with full preflight

A pull request SHALL be ready for review when `bash workspace/sdlc/preflight.sh --full` has passed on that branch. A preflight run without `--full` SHALL NOT be described as sufficient to open a pull request. GitHub Actions SHALL NOT re-run that suite on `pull_request`.

#### Scenario: Default preflight is not the PR gate

- **WHEN** `workspace/sdlc/preflight.sh` help text or its end-of-run note is read for a run that did not pass `--full`
- **THEN** the text says that run is not sufficient to open a pull request and names `bash workspace/sdlc/preflight.sh --full`

#### Scenario: Skipped full suites do not promise a pull-request rerun

- **WHEN** `workspace/sdlc/preflight.sh` skips a `--full` suite
- **THEN** the skip reason does not say that CI will run that suite on the pull request

### Requirement: Heavy product workflows run on version tags and manual dispatch

The workflows `ci.yml`, `frontend-ci.yml`, `playwright-e2e.yml`, `pr-validation.yml`, `pages-ci.yml`, `openapi-contract.yml`, `database-vv.yml`, `sdlc-process.yml`, and `test-coverage-report.yml` SHALL trigger on `push` tags `v*` and `notaire-v*` and on `workflow_dispatch`. They SHALL NOT trigger on `pull_request`, on push to a branch, or on a schedule.

#### Scenario: CI workflow trigger

- **WHEN** `.github/workflows/ci.yml` triggers are read
- **THEN** `push.tags` includes `v*` and `notaire-v*`, `workflow_dispatch` is present, and `pull_request`, branch `push`, and `schedule` are absent

#### Scenario: Playwright has no weekday cron

- **WHEN** `.github/workflows/playwright-e2e.yml` triggers are read
- **THEN** `schedule` is absent and the workflow does not contain the cron `0 6 * * 1-5`

#### Scenario: Process checks have no daily cron

- **WHEN** `.github/workflows/sdlc-process.yml` triggers are read
- **THEN** `schedule` is absent, `push.branches` does not include `main`, and `push.tags` includes `v*` and `notaire-v*`

### Requirement: CodeQL keeps a weekly scan and release tags

`.github/workflows/codeql.yml` SHALL NOT trigger on `pull_request` or on push to a branch. It SHALL keep the weekly Monday schedule, and it SHALL also trigger on tags `v*` and `notaire-v*` and on `workflow_dispatch`.

#### Scenario: CodeQL schedule remains

- **WHEN** `.github/workflows/codeql.yml` triggers are read
- **THEN** `schedule` includes `30 6 * * 1`, `pull_request` is absent, and `push.tags` includes `v*` and `notaire-v*`

### Requirement: Operational schedules stay

`cleanup-actions.yml`, `dast-zap.yml`, `performance-test.yml`, and `backup-restore-smoke.yml` SHALL keep their existing schedule triggers. Release Please SHALL keep `push` to `main`.

#### Scenario: Release Please still runs on main

- **WHEN** `.github/workflows/release-please.yml` triggers are read
- **THEN** `push.branches` includes `main` and `workflow_dispatch` is present

### Requirement: The documentation site deploys without a product release

`deploy-github-page.yml` SHALL deploy on push to `main` and on `workflow_dispatch`. It SHALL NOT wait for a `workflow_run` of CI on `main`.

#### Scenario: Pages deploy trigger

- **WHEN** `.github/workflows/deploy-github-page.yml` triggers are read
- **THEN** `push.branches` includes `main`, `workflow_dispatch` is present, and `workflow_run` is absent
