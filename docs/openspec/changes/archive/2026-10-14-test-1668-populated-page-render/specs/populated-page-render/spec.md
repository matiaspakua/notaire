<!-- Governed by CONSTITUTION.md. CU76, issue #1668. Evidence: vitest --coverage on 2026-10-14. -->

## ADDED Requirements

### Requirement: Every dashboard page renders with data

The frontend unit suite MUST render each dashboard page with a populated list response and MUST fail when a page throws. The Vitest coverage floors MUST be at least 62% statements, 55% branches, 57% functions and 63% lines.

#### Scenario: a populated list renders

- **WHEN** a dashboard page receives one row per list request
- **THEN** it renders without an application error

#### Scenario: the floors follow the measurement

- **WHEN** the configured thresholds are compared with the documented floors
- **THEN** they are equal or higher
