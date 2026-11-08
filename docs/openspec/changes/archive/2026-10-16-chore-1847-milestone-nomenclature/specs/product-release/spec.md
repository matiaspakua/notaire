## REMOVED Requirements

### Requirement: A semver maps to a delivery train and is not the milestone title

**Reason**: The dated release-blocker train is replaced by one version-first train per minor (#1847).
**Migration**: Use "A semver maps to the train with its version prefix".

## ADDED Requirements

### Requirement: A semver maps to the train with its version prefix

A product version `X.Y.Z` SHALL map to the milestone whose title starts with `vX.Y · `. Every patch of a minor SHALL map to the same train. A minor with no train SHALL fail closed. A release SHALL NOT close catalog issues.

#### Scenario: Patches share their minor's train

- **WHEN** the train is derived for `0.2.0` and for `0.2.3`
- **THEN** both results are `v0.2 · Release identity & product forms (2026-10)`

#### Scenario: A minor with no train fails

- **WHEN** the train is derived for `1.1.0`
- **THEN** the derivation raises an error
