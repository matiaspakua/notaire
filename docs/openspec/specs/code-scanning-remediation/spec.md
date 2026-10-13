# code-scanning-remediation Specification

## Purpose
Ensure the security posture detected by code scanning is remediated safely and
that the same vulnerability classes fail the local gate before pull-request CI.
## Requirements
### Requirement: CD jobs use explicit least-privilege permissions

Every CD workflow job SHALL declare the minimum GitHub token permissions needed
to perform its work, with write access granted only to a job that needs it.

#### Scenario: Read-only CD jobs declare read-only permissions

- **WHEN** a CD job only checks release identity, updates a registry description,
  or produces a report
- **THEN** that job declares explicit read-only permissions and does not inherit
  repository-write access

#### Scenario: Release publication retains only its required write access

- **WHEN** the release-publication job attaches assets to a GitHub release
- **THEN** it retains its explicitly declared release-content permission without
  granting that permission to unrelated jobs

### Requirement: Dynamic regex input is quoted as data

Text interpolated into a dynamically constructed regular expression SHALL be
escaped as literal data before matching.

#### Scenario: URL text containing a regex metacharacter is matched literally

- **WHEN** a tested URL segment contains a regular-expression metacharacter
- **THEN** the URL assertion matches that exact segment rather than interpreting
  the metacharacter as regex syntax

#### Scenario: Escaping preserves ordinary URL matching

- **WHEN** a tested URL segment contains no regular-expression metacharacter
- **THEN** the URL assertion continues to match the expected route

