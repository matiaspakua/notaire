# semver-versioned-releases — delta

## Purpose

Automated semver releases via release-please.

## MODIFIED Requirements

### Requirement: Release PR identity

The release-please workflow SHALL authenticate with the default `GITHUB_TOKEN` and SHALL start from a bootstrap commit.

#### Scenario: Workflow uses the default token

- **WHEN** release-please.yml is parsed
- **THEN** the action's token input is github.token and references no secret

#### Scenario: History is bootstrapped

- **WHEN** release-please-config.json is parsed
- **THEN** bootstrap-sha is a full commit SHA

#### Scenario: Runbook documents the setting and manual checks

- **WHEN** RELEASE.md is read
- **THEN** it names the Actions-PR setting and explains that GITHUB_TOKEN PRs do not trigger other workflows
