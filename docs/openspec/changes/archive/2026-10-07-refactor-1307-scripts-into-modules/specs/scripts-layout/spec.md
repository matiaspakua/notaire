## Purpose

Scripts live in the module they serve. Source: #1307; owner CU76.

## ADDED Requirements

### Requirement: Moved scripts have one location and no stale references

A script moved out of `scripts/` MUST exist at its new path, MUST NOT exist at the old one, and no tracked file outside history MAY reference the old path.

#### Scenario: Moved scripts exist at their new path

- **WHEN** the layout guard runs
- **THEN** every script in its moved table exists at the new path

#### Scenario: Moved scripts are gone from the old path

- **WHEN** the layout guard runs
- **THEN** none of the old paths exists

#### Scenario: No file references an old path

- **WHEN** the layout guard scans tracked files outside history (CHANGELOG, archives, deprecated)
- **THEN** no file contains an old path
