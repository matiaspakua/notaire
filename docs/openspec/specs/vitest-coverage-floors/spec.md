# vitest-coverage-floors Specification

## Purpose
TBD - created by archiving change test-2249-raise-vitest-floors. Update Purpose after archive.

## Requirements

### Requirement: The Vitest floors track the measurement

The configured Vitest thresholds MUST stay at or above the documented floors and MUST leave at least one point of headroom under a fresh measurement.

#### Scenario: the guard fails when a floor is lowered

- **WHEN** a configured threshold is below its documented floor
- **THEN** the thresholds guard test fails
