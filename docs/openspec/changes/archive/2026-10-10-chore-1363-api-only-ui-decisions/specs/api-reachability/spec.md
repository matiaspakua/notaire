# api-reachability — delta

## Purpose

Every OpenAPI endpoint is called from the UI or allowlisted with a decided reason.

## MODIFIED Requirements

### Requirement: Allowlist entries carry a decided reason

Every allowlisted endpoint SHALL give a decided reason; the Owner's API-only decisions of 2026-10-09 SHALL be recorded as API-only.

#### Scenario: No pending decision

- **WHEN** the allowlist is read
- **THEN** no entry says `Owner to reclassify`

#### Scenario: Decided entries

- **WHEN** the report and the three manual movement endpoints are looked up
- **THEN** each reason says API-only
