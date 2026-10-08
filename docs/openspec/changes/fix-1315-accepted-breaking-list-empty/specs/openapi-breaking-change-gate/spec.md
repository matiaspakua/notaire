# openapi-breaking-change-gate — delta

## Purpose

OpenAPI breaking changes fail CI unless accepted by the Owner, and accepted entries do not outlive their pull request.

## MODIFIED Requirements

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

### Requirement: Same gate locally and in CI

The stale-entry check SHALL run in `openapi-contract.yml` on pull requests and in `preflight.sh` when oasdiff is installed.

#### Scenario: CI and preflight run the check

- **WHEN** the workflow and preflight are inspected
- **THEN** both run `workspace/sdlc/check-accepted-breaking-changes.py`, CI with checksum-verified oasdiff 1.33.0
