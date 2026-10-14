# dashboard-page-smoke Specification

## Purpose
TBD - created by archiving change test-1667-dashboard-smoke-ratchet. Update Purpose after archive.

## Requirements

### Requirement: Dashboard pages render in a unit test and the floor follows the measurement

A unit test MUST render each route-param-free dashboard page against an empty API and fail when a page throws or shows an application error. The Vitest coverage thresholds MUST be at least 59% statements, 52% branches, 49% functions and 60% lines, and the guard test MUST document the same floors.

#### Scenario: every dashboard page renders against an empty API

- **WHEN** each dashboard page is rendered with every endpoint answering an empty list
- **THEN** it renders and settles without an application error

#### Scenario: configured thresholds meet the documented floors

- **WHEN** the thresholds in `vitest.config.ts` are compared with the documented floors
- **THEN** each is at least the documented floor

#### Scenario: documented floors reflect the measurement

- **WHEN** the documented floors are read
- **THEN** they are 59, 52, 49 and 60
