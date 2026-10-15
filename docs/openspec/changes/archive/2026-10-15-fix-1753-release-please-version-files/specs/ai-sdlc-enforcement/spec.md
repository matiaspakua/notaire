<!-- Governed by CONSTITUTION.md. CU76, issue #1753. -->

## ADDED Requirements

### Requirement: The release-please PR is exempt from the exception gate

The exception gate SHALL accept a PR without an OpenSpec change when its author
is `github-actions[bot]` and its head branch starts with `release-please--`. Any
other combination still needs an OpenSpec change or the human `sdlc-exception` label.

#### Scenario: Release-please PR passes

- **WHEN** the author is `github-actions[bot]` and the head branch is `release-please--branches--main--components--notaire`
- **THEN** `check-sdlc-exception.sh` exits 0

#### Scenario: Other actions-bot PR still needs approval

- **WHEN** the author is `github-actions[bot]` and the head branch does not start with `release-please--`
- **THEN** `check-sdlc-exception.sh` fails

#### Scenario: Human on a release-please branch still needs approval

- **WHEN** the head branch starts with `release-please--` but the author is not `github-actions[bot]`
- **THEN** `check-sdlc-exception.sh` fails
