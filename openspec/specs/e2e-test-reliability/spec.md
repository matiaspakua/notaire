# e2e-test-reliability Specification

## Purpose
Define Playwright E2E reliability rules for Notaire so suites prove behavior
under CU76 – Quality Assurance and Testing Infrastructure: arrange their own
data, wait on real UI conditions, cite intentional skips, and avoid retry/sleep
budgets that hide flakiness (#1066).
## Requirements
### Requirement: Self-arranging workflow E2E data

Workflow editor and assignment E2E suites (TS-0021, TS-0022) MUST create any
required workflow / tipo-tramite fixtures via API helpers before exercising the
UI. They MUST NOT call `test.skip()` solely because the table or editor list is
empty.

#### Scenario: Workflow editor arranges data instead of skipping

- **WHEN** TS-0021 editor tests run against an environment with no pre-existing
  workflow rows
- **THEN** the suite creates the needed workflow (via API helpers) and executes
  the editor assertions without a data-missing `test.skip()`

#### Scenario: Workflow assignment arranges data instead of skipping

- **WHEN** TS-0022 assignment tests run against an environment with no
  tipo-tramite rows
- **THEN** the suite creates the needed row (via API helpers) and executes the
  assignment assertions without a data-missing `test.skip()`

#### Scenario: Arrange failure fails the test

- **WHEN** API arrange/seed for TS-0021 or TS-0022 fails
- **THEN** the test fails with an error (does not skip as “pass”)

### Requirement: Web-first waits without fixed sleeps

Touched production/QA E2E specs named by #1066 MUST NOT use `waitForTimeout` as
a correctness wait. They SHALL assert on visible UI state, URL, or network
responses instead.

#### Scenario: Localization suite waits on locale signal

- **WHEN** TS-0040 switches language
- **THEN** it asserts a locale-visible change (text, `lang`, or equivalent)
  without `waitForTimeout`

#### Scenario: Icons QA suite waits on UI readiness

- **WHEN** TS-0043 verifies icons across pages
- **THEN** it waits on locators / load conditions without a fixed 2000 ms sleep

### Requirement: Intentional skips cite open issues

Any remaining intentional feature-gap `test.skip` in TS-0014, TS-0016, TS-0017,
or TS-0020 MUST include a reference to an open GitHub issue in the skip reason
or title.

#### Scenario: Feature-gap skip cites an open issue

- **WHEN** a test in TS-0014, TS-0016, TS-0017, or TS-0020 remains intentionally
  skipped for a product/UI gap
- **THEN** the skip reason or title includes an open `#issue` reference

### Requirement: Reduced CI retry budget with flake visibility

Playwright CI configuration MUST use at most one retry. Flaky failures MUST
remain inspectable via Playwright traces/artifacts (and documented triage
pointers).

#### Scenario: CI retries are at most one

- **WHEN** Playwright runs with `CI` set
- **THEN** the configured retry count is `1` (not `2` or higher)

#### Scenario: Retry retains failure evidence

- **WHEN** a test fails once and is retried in CI
- **THEN** trace/screenshot/video retention settings still allow triage of the
  first failure (e.g. `trace: on-first-retry` or equivalent artifacts)

