# docs-quality — delta

## Purpose

Documentation checks that run on every PR.

## MODIFIED Requirements

### Requirement: Docs checks pass on main

The repository SHALL keep a single section per change type under [Unreleased] and SHALL use absolute links in release templates, so docs/tests pass.

#### Scenario: Duplicate section

- **WHEN** CHANGELOG [Unreleased]
- **THEN** one ### Changed

#### Scenario: Placeholder link

- **WHEN** RELEASE.md template
- **THEN** absolute URL accepted by the link check
