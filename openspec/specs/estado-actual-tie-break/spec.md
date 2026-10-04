# estado-actual-tie-break Specification

## Purpose

Make the current-status endpoint deterministic. Source: #1198; owner CU13.

## Requirements

### Requirement: The latest history row wins, ties broken by id

`GET /api/v1/gestiones/{id}/estado-actual` MUST return the history row with the latest date and,
when several rows share that date, the one with the highest id.

#### Scenario: Identical dates return the later insert

- **WHEN** a gestion has two history rows with the same date
- **THEN** the endpoint returns the row with the higher id

#### Scenario: Distinct dates still return the latest date

- **WHEN** a gestion has history rows with different dates
- **THEN** the endpoint returns the row with the latest date
