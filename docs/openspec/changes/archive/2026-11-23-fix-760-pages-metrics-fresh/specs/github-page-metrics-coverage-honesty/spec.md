# github-page-metrics-coverage-honesty Specification Delta

## Purpose

Extend the coverage honesty spec so that `e2eTests` and `apiEndpoints` are computed fresh each run from Playwright and Bruno artifacts, and any fallback value is visibly marked stale.

## ADDED Requirements

### Requirement: e2eTests SHALL be computed fresh from Playwright results each run

The shared metrics generator SHALL compute `coverage.e2eTests` from the Playwright JSON reporter output (`results.json`) on every run, counting the total test specs (`stats.expected + stats.unexpected + stats.skipped + stats.flaky`). When the Playwright artifact is missing or unparseable, the generator SHALL fall back to the last live value and record `"e2eTests"` in `coverage.staleFields`.

#### Scenario: Fresh e2eTests when Playwright results are present

- **GIVEN** a Playwright `results.json` artifact with `stats.expected = 42`, `stats.unexpected = 1`, `stats.skipped = 0`, `stats.flaky = 0`
- **WHEN** the shared metrics generator runs
- **THEN** `coverage.e2eTests` equals `43` and `"e2eTests"` is NOT in `coverage.staleFields`

#### Scenario: Stale fallback for e2eTests when Playwright results are missing

- **GIVEN** no Playwright `results.json` artifact and a live `metrics.json` with `coverage.e2eTests = 40` and `lastUpdated` measured
- **WHEN** the shared metrics generator runs
- **THEN** `coverage.e2eTests` equals `40` and `"e2eTests"` IS present in `coverage.staleFields`

### Requirement: apiEndpoints SHALL be computed fresh from the Bruno report each run

The shared metrics generator SHALL compute `coverage.apiEndpoints` from the Bruno CLI JSON reporter output (`bruno-results.json`) on every run, counting the total requests across all iterations (`summary.totalRequests`). When the Bruno artifact is missing or unparseable, the generator SHALL fall back to the last live value and record `"apiEndpoints"` in `coverage.staleFields`.

#### Scenario: Fresh apiEndpoints when Bruno report is present

- **GIVEN** a Bruno `bruno-results.json` artifact with one iteration whose `summary.totalRequests = 25`
- **WHEN** the shared metrics generator runs
- **THEN** `coverage.apiEndpoints` equals `25` and `"apiEndpoints"` is NOT in `coverage.staleFields`

#### Scenario: Stale fallback for apiEndpoints when Bruno report is missing

- **GIVEN** no Bruno `bruno-results.json` artifact and a live `metrics.json` with `coverage.apiEndpoints = 20` and `lastUpdated` measured
- **WHEN** the shared metrics generator runs
- **THEN** `coverage.apiEndpoints` equals `20` and `"apiEndpoints"` IS present in `coverage.staleFields`

### Requirement: Fallback values MUST be visibly marked stale in the UI

The Coverage UI component SHALL check `coverage.staleFields` and render a stale indicator (badge, label, or date) next to any coverage metric whose field name appears in the array. A stale value MUST NOT be presented as current.

#### Scenario: UI renders stale badge for a fallback e2eTests value

- **GIVEN** metrics whose `coverage.staleFields` contains `"e2eTests"`
- **WHEN** the Coverage component renders the E2E (Playwright) metric card
- **THEN** a stale indicator is visible on that card

#### Scenario: UI does not render stale badge for a fresh value

- **GIVEN** metrics whose `coverage.staleFields` does NOT contain `"e2eTests"`
- **WHEN** the Coverage component renders the E2E (Playwright) metric card
- **THEN** no stale indicator is visible on that card

### Requirement: staleFields SHALL be an empty array when all values are fresh

When both `e2eTests` and `apiEndpoints` are computed successfully from their respective artifacts, `coverage.staleFields` SHALL be an empty JSON array (`[]`).

#### Scenario: staleFields is empty when both values are fresh

- **GIVEN** both Playwright `results.json` and Bruno `bruno-results.json` artifacts are present and parseable
- **WHEN** the shared metrics generator runs
- **THEN** `coverage.staleFields` equals `[]`
