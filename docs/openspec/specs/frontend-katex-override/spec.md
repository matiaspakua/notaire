# frontend-katex-override Specification

## Purpose

Ensure the Next.js frontend lockfile resolves `katex` to a patched release so
Dependabot's alert for that package is cleared. Source: #1943; CU78.

## Requirements

### Requirement: Frontend lockfile pins a patched katex

The frontend npm dependency graph SHALL resolve `katex` to version **0.18.2 or newer**.
Because `micromark-extension-math@3.1.0` (via `markdownlint@0.42.0` and `markdownlint-cli2@0.24.0`)
declares `katex@^0.16.0` and no newer release accepts the patched katex, `frontend/package.json`
SHALL declare an npm `overrides` entry for `katex` and commit the refreshed lockfile. Source: #1943; CU78.

#### Scenario: package.json declares a katex override

- **WHEN** `frontend/package.json` is inspected
- **THEN** `overrides.katex` forces a range whose floor is `>=0.18.2`

#### Scenario: lockfile resolves katex to a patched version

- **WHEN** `frontend/package-lock.json` is inspected for every `katex` package entry
- **THEN** each resolved version is `>=0.18.2`, outside the advisory range `>= 0.11.0 < 0.18.2`

#### Scenario: markdownlint-cli2 still lints the repository Markdown

- **WHEN** `npm ci` is run under `frontend/` and `markdownlint-cli2` lints Markdown containing `$$` math
- **THEN** install completes, `npm ls katex` reports the overridden version, and the linter runs without a module error
