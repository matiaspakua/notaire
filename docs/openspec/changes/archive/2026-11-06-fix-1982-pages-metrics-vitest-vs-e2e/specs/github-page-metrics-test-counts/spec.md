# github-page-metrics-test-counts Delta

## ADDED Requirements

### Requirement: Vitest and Playwright counts are distinct

`frontendTestClasses` SHALL count Vitest files under `frontend/src`. Playwright specs under `testing/e2e/tests` SHALL be published as `e2eSpecFiles`. The Coverage section SHALL not label Vitest counts as E2E suites.

#### Scenario: Generator writes separate Vitest and E2E counts

- **GIVEN** the metrics generator script
- **WHEN** metrics.json is produced
- **THEN** `frontendTestClasses` equals the Vitest file count and `e2eSpecFiles` equals the Playwright spec count

#### Scenario: Coverage UI uses the E2E field for Playwright

- **GIVEN** SiteMetrics with distinct frontendTestClasses and e2eSpecFiles
- **WHEN** the Coverage section renders
- **THEN** the Playwright line shows e2eSpecFiles
