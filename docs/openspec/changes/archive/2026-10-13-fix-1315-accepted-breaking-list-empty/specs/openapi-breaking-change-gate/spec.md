<!-- Superseded by #1389 (accepted-breaking-changes.d/): this requirement was not kept in the main spec when archived on 2026-10-13 (#1623). -->
# openapi-breaking-change-gate — delta

## Purpose

OpenAPI breaking changes fail CI unless accepted by the Owner, and accepted entries do not outlive their pull request.

## ADDED Requirements

### Requirement: Accepted entries do not outlive their pull request

Every entry in `accepted-breaking-changes.txt` SHALL match a breaking change between the base spec and the revision; CI SHALL fail a pull request that keeps an entry whose break is already in the base.

#### Scenario: Empty list

- **WHEN** the list has no entries
- **THEN** the check passes

#### Scenario: Live entry

- **WHEN** a pull request introduces a break and lists it
- **THEN** the check passes

#### Scenario: Stale entry

- **WHEN** a later pull request still carries an entry whose break is already on main
- **THEN** the check fails and names the entry
