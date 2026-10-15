<!-- Governed by CONSTITUTION.md. CU76, issue #1753. Evidence: #1597 and #1718 needed manual POM and OpenAPI bumps. -->

## ADDED Requirements

### Requirement: The release PR bumps every product version source

Release-please SHALL bump both Maven POMs and the OpenAPI `info.version` to the
manifest version on its release branch, so the release PR passes the version
coherence and OpenAPI contract gates without manual commits.

#### Scenario: POM XPaths match namespaced POMs

- **WHEN** the POM entries in `release-please-config.json` are read
- **THEN** each XPath uses `local-name()` steps that resolve to `project/version` and `project/parent/version`, whose current value equals the manifest

#### Scenario: OpenAPI info.version is synced on the release branch

- **WHEN** release-please opens or updates its PR
- **THEN** the workflow runs `sync-openapi-version.py` with the manifest version on the release branch, which changes only the top-level `info.version` line, is idempotent and rejects a non-SemVer version
