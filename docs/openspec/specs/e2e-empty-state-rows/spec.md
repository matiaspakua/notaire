# e2e-empty-state-rows Specification

## Purpose

The shared status-badge E2E check counts rows of a list. An empty list shows one placeholder row, which is not data.

## Requirements

### Requirement: The status-badge check counts data rows only

The E2E check that every row of a list shows a status badge MUST count only data rows. The empty-state placeholder
row of an empty list MUST NOT be counted, and an empty list MUST skip the check rather than fail it.

#### Scenario: an empty list

- **WHEN** a list has no data rows and shows its empty state
- **THEN** the check is skipped and nothing fails

#### Scenario: a populated list

- **WHEN** a list has data rows
- **THEN** every data row has a status badge with a tone, an icon and text, and the placeholder row is not one of them
