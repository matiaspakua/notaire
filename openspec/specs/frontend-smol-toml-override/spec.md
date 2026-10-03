# frontend-smol-toml-override Specification

## Purpose
Ensure the Next.js frontend lockfile resolves `smol-toml` to a patched release
so Dependabot’s high alert for that package is cleared. Source: #1046; CU78.
## Requirements
### Requirement: Frontend lockfile pins a patched smol-toml

The frontend npm dependency graph MUST resolve `smol-toml` to version
**1.7.1 or newer** (prefer **1.9.0** / current latest at implement time).
Because `markdownlint-cli2@0.23.3` declares `smol-toml@1.8.0` and no newer
`markdownlint-cli2` release is available, the repository MUST declare an npm
`overrides` (or equivalent lockfile force) for `smol-toml` and commit the
refreshed `frontend/package-lock.json`.

#### Scenario: package.json declares a smol-toml override

- **WHEN** `frontend/package.json` is inspected
- **THEN** it contains an `overrides` (or documented equivalent) entry that
  forces `smol-toml` to `>=1.7.1` (prefer `^1.9.0` / `1.9.0`)

#### Scenario: lockfile resolves smol-toml to a patched version

- **WHEN** `frontend/package-lock.json` is inspected for the `smol-toml`
  package entry
- **THEN** the resolved version is `>=1.7.1` and is not a vulnerable range
  for GHSA-7w5x-hrqm-74c2 (`<=1.7.0`)

#### Scenario: npm install keeps the override

- **WHEN** `npm ci` (or `npm install`) is run under `frontend/`
- **THEN** `npm ls smol-toml` reports the overridden patched version and
  install completes successfully

