# ci-no-bot-report-commits Specification

## Purpose
TBD - created by archiving change fix-1041-no-bot-commits-main. Update Purpose after archive.
## Requirements
### Requirement: Workflows never commit generated CI reports into the repo

CI, CD, Playwright E2E, and PR validation workflows MUST NOT create git
commits that add or update files under `docs/wiki/cicd-reports/` (or
equivalent generated report paths) on `main` or on pull-request heads.

#### Scenario: CI publish-reports has no git commit/push

- **WHEN** `.github/workflows/ci.yml` job `publish-reports` (or its successor)
  runs after a push to `main`
- **THEN** that job MUST NOT run `git commit` or `git push` for report files,
  and MUST NOT configure a `CI Bot` identity for the purpose of committing
  reports into the application repository

#### Scenario: CD publish-report has no git commit/push

- **WHEN** `.github/workflows/cd.yml` job `publish-report` (or its successor)
  runs after a successful CD path
- **THEN** that job MUST NOT run `git commit` or `git push` of
  `docs/wiki/cicd-reports/` content onto `main`

#### Scenario: Playwright coverage-report has no git commit/push

- **WHEN** `.github/workflows/playwright-e2e.yml` job `coverage-report` runs
  on a non-`pull_request` event
- **THEN** that job MUST NOT run `git commit` or `git push` of E2E coverage
  markdown into the application repository

#### Scenario: PR validation does not commit wiki reports onto the PR head

- **WHEN** `.github/workflows/pr-validation.yml` completes on a pull request
- **THEN** it MUST NOT commit wiki/CI report markdown onto the PR head (reports
  remain workflow artifacts and/or PR comments only)

### Requirement: Reports remain discoverable without git history

Generated CI/CD/E2E reports MUST still be available to humans via at least one
of: workflow artifacts, `$GITHUB_STEP_SUMMARY`, or GitHub Pages — not via new
commits on `main`.

#### Scenario: Reports published as artifact and/or job summary and/or Pages

- **WHEN** CI, CD, or Playwright E2E finishes a run that previously would have
  committed a markdown report
- **THEN** the equivalent report content is uploaded as a workflow artifact
  and/or written to the job summary and/or published through the GitHub Pages
  deploy path, with no accompanying git commit of that report into the repo

### Requirement: Report directory untracked and ignored; least privilege

`docs/wiki/cicd-reports/` MUST NOT be tracked by git and MUST be listed in
`.gitignore`. Jobs whose only former write need was report commits MUST NOT
request `contents: write`.

#### Scenario: cicd-reports path is git-ignored and untracked

- **WHEN** the repository tree after this change is inspected
- **THEN** `.gitignore` contains `docs/wiki/cicd-reports/` (or an equivalent
  pattern covering that directory) and no files under that path are tracked in
  the git index

#### Scenario: Report-publish jobs drop contents write

- **WHEN** the former report-commit jobs in `ci.yml`, `cd.yml`, and
  `playwright-e2e.yml` are inspected
- **THEN** those jobs do not set `permissions.contents: write` (the CD
  `release` job MAY retain `contents: write` solely for GitHub Releases)

