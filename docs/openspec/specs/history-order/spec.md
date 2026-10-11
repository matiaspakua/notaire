# history-order Specification

## Purpose
Deterministic chronological order of a management's history.
## Requirements
### Requirement: Total order of a management's history

Every endpoint that returns or interprets a management's history SHALL order it by event date ascending and, for entries on the same date, by idHistory ascending (insertion order).

#### Scenario: Bitácora lists same-day entries in insertion order

- **WHEN** a management gets two state changes on the same day and a client calls GET /api/v1/managements/{id}/history
- **THEN** the entries come back in the order they were recorded, on every call

#### Scenario: History by management is sorted

- **WHEN** a client calls GET /api/v1/history/management/{id}
- **THEN** the entries come back by event date ascending, then idHistory ascending

#### Scenario: Workflow trace picks the last same-day entry as current

- **WHEN** the two newest entries share a date
- **THEN** the workflow trace marks the status of the entry with the higher idHistory as in progress

