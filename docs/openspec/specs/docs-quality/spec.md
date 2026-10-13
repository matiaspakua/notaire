# docs-quality Specification

## Purpose

Documentation checks that run on every PR. Source: #1582; archived change(s) `2026-10-13-fix-1582-docs-tests-main`.

## Requirements
### Requirement: Docs checks pass on main

The repository SHALL keep a single section per change type under [Unreleased] and SHALL use absolute links in release templates, so docs/tests pass.

#### Scenario: Duplicate section

- **WHEN** CHANGELOG [Unreleased]
- **THEN** one ### Changed

#### Scenario: Placeholder link

- **WHEN** RELEASE.md template
- **THEN** absolute URL accepted by the link check

