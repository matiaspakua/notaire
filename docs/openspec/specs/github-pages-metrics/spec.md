# github-pages-metrics Specification

## Purpose

TBD - created by archiving change fix-2507-metrics-gh-error. Update Purpose after archive.

## Requirements

### Requirement: The Pages metrics stay valid JSON when the GitHub API fails

`workspace/ci/generate-github-page-metrics.sh` SHALL write a valid `metrics.json` when `gh api` fails or answers
with anything but a number: the pull-request count SHALL fall back to the live deployed value, or 0 when there is
none. Its tests SHALL stub `gh` and make no network call.

#### Scenario: The search API returns an error

- **WHEN** `gh api` prints an error object and exits non-zero
- **THEN** `metrics.json` parses and `pullRequests` is the live value

#### Scenario: Tests do not reach GitHub

- **WHEN** the metrics tests run
- **THEN** a stub `gh` on PATH answers instead of the GitHub API
