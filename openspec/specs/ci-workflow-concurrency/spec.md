# ci-workflow-concurrency Specification

## Purpose
TBD - created by archiving change ci-cancel-in-progress-1148. Update Purpose after archive.
## Requirements
### Requirement: Same-ref CI concurrency cancels superseded runs

GitHub Actions workflows for backend CI and Playwright MUST cancel in-progress
runs on the same branch/ref when a newer push arrives, so the latest head is
not starved by superseded jobs.

#### Scenario: CI workflow enables cancel-in-progress

- **WHEN** `.github/workflows/ci.yml` defines a `concurrency` block for the PR ref
- **THEN** `cancel-in-progress` is `true`

#### Scenario: Playwright workflow enables cancel-in-progress

- **WHEN** `.github/workflows/playwright-e2e.yml` defines a `concurrency` block for the PR ref
- **THEN** `cancel-in-progress` is `true`

#### Scenario: Pages deploy does not cancel mid-flight

- **WHEN** `.github/workflows/deploy-github-page.yml` defines concurrency
- **THEN** `cancel-in-progress` remains `false`

