# github-page-metrics-coverage-honesty Specification

## Purpose

Pages portfolio coverage fields must never present a fabricated numeric zero when JaCoCo or Vitest measurement artifacts are missing (CU76 / #2203).

## Requirements

### Requirement: Missing measurement artifacts MUST NOT publish zero coverage

When `coverage-snapshot/coverage-snapshot.json` is absent, the shared metrics generator SHALL NOT write numeric `0` for `coverage.backendInstruction` or `coverage.backendBranch`. When `frontend-coverage-dl/coverage/coverage-summary.json` is absent, it SHALL NOT write numeric `0` for `coverage.frontendComponent`. Unmeasured fields SHALL be JSON `null`, and `coverage.lastUpdated` SHALL be `unknown` when no measured backend stamp is available.

#### Scenario: Missing snapshot and missing live do not publish zero coverage

- **GIVEN** no JaCoCo snapshot file and no reachable live metrics (or an empty live document)
- **WHEN** the shared metrics generator runs
- **THEN** `coverage.backendInstruction` and `coverage.backendBranch` are JSON `null` (not `0`) and `coverage.lastUpdated` is `unknown`

#### Scenario: Poisoned live zeros with unknown stamp are not copied forward

- **GIVEN** no JaCoCo snapshot and a live `metrics.json` whose coverage has `backendInstruction: 0`, `backendBranch: 0`, and `lastUpdated: "unknown"`
- **WHEN** the shared metrics generator runs
- **THEN** those backend fields are written as JSON `null` (the live zeros are not reused)

#### Scenario: Present JaCoCo snapshot updates backend fields

- **GIVEN** a JaCoCo snapshot with non-zero backend instruction and branch percentages and a dated `lastUpdated`
- **WHEN** the shared metrics generator runs
- **THEN** `coverage.backendInstruction`, `coverage.backendBranch`, and `coverage.lastUpdated` match the snapshot

#### Scenario: Present frontend summary updates frontendComponent

- **GIVEN** a Vitest `coverage-summary.json` with a statements percentage
- **WHEN** the shared metrics generator runs
- **THEN** `coverage.frontendComponent` equals that percentage floored

#### Scenario: Measured live fallback reused when snapshot missing

- **GIVEN** no JaCoCo snapshot and a live `metrics.json` whose coverage has a non-`unknown` `lastUpdated` and numeric backend percentages
- **WHEN** the shared metrics generator runs
- **THEN** those live backend percentages and `lastUpdated` are reused

### Requirement: Portfolio consumers MUST treat null coverage as unmeasured

Site loaders and coverage displays SHALL treat JSON `null` coverage percentages and `lastUpdated: "unknown"` as not measured. They MUST NOT render those fields as a measured `0%`.

#### Scenario: UI treats null / unknown as not measured

- **GIVEN** metrics whose coverage percentages are `null` or whose `lastUpdated` is `unknown`
- **WHEN** Results / Numbers coverage helpers evaluate the snapshot
- **THEN** the coverage is reported as not measured (for example `n/a`), not as `0%`

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
