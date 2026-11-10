# github-page-metrics-coverage-honesty Specification

## Purpose

Pages portfolio coverage fields must never present a fabricated numeric zero when JaCoCo or Vitest measurement artifacts are missing (CU76 / #2203).

## ADDED Requirements

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
