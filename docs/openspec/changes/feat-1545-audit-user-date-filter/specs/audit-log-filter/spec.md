# audit-log-filter — delta

## Purpose

Filtering the audit log by user and date range.

## MODIFIED Requirements

### Requirement: Audit log user and date filter

The audit log SHALL filter by user and an inclusive date range combined with the module filter, server-side and paged, and SHALL reject an inverted or malformed range with 400.

#### Scenario: User and range

- **WHEN** records of two users on several days
- **THEN** only the chosen user's records within the inclusive range are returned

#### Scenario: Combined

- **WHEN** module and idUser are both sent
- **THEN** both filters apply

#### Scenario: Open-ended

- **WHEN** only from or only to
- **THEN** the range is open on the other side

#### Scenario: Invalid

- **WHEN** from after to, or a malformed date
- **THEN** 400; the UI shows an inline error and sends nothing

#### Scenario: URL

- **WHEN** filters are changed
- **THEN** they are written to the URL and the page resets
