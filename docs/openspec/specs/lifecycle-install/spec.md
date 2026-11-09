# lifecycle-install Specification

## Purpose
TBD - created by archiving change fix-2153-install-abs-path. Update Purpose after archive.

## Requirements

### Requirement: Relative-only install destinations
The system SHALL reject absolute and home-relative (`~/`) `--path` values for install/update/uninstall with exit code 2, and SHALL NOT create an in-repo directory by stripping a leading slash.

#### Scenario: Absolute path refused
- **WHEN** an operator runs `aisdlc install --path /tmp/kit`
- **THEN** the command exits 2 and no `<repo>/tmp/kit` tree is created
