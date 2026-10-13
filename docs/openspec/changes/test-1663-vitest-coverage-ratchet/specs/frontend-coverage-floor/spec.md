<!-- Governed by CONSTITUTION.md. CU76, issue #1663. Evidence: floors 14/9/10/14 versus measured 52.07/48.85/46.22/52.39. -->

## ADDED Requirements

### Requirement: The Vitest coverage floor follows measured coverage

`frontend/vitest.config.ts` MUST enforce coverage thresholds of at least 50% statements, 46% branches, 44% functions and 50% lines, and the guard test MUST document the same floors so lowering them fails the suite.

#### Scenario: configured thresholds meet the documented floors

- **WHEN** the thresholds in `vitest.config.ts` are compared with the documented floors
- **THEN** each configured value is at least the documented floor

#### Scenario: documented floors reflect the measurement

- **WHEN** the documented floors are read
- **THEN** they are 50, 46, 44 and 50
