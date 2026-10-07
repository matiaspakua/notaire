# semver-versioned-releases — delta

## Purpose

Automated semver releases via release-please.

## MODIFIED Requirements

### Requirement: Release PR identity

The release-please workflow SHALL authenticate with `RELEASE_PLEASE_TOKEN` when present and SHALL start from a bootstrap commit.

#### Scenario: Workflow uses the dedicated token

- **WHEN** release-please.yml is parsed
- **THEN** the action's token input references secrets.RELEASE_PLEASE_TOKEN

#### Scenario: History is bootstrapped

- **WHEN** release-please-config.json is parsed
- **THEN** bootstrap-sha is a full commit SHA

#### Scenario: Runbook names the secret

- **WHEN** RELEASE.md is read
- **THEN** it explains how to create RELEASE_PLEASE_TOKEN
