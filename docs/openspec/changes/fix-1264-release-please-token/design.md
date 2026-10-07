# Design

## Context

Run log: `GitHub Actions is not permitted to create or approve pull requests`.

## Goals / Non-Goals

Goal: Release Please opens its PR. Non-goal: a PAT or GitHub App (not needed once the setting is enabled).

## Decisions

Use the default token per the Owner's choice (setting enabled); document the manual close/reopen step for required checks; bootstrap at the #1043 commit.

## Riesgos / Trade-offs

Release PRs start without required checks until the Owner closes and reopens them; disabling the repository setting breaks the workflow again. bootstrap-sha choice is the Owner's to confirm.

## Testing Strategy

Three new guard tests in `scripts/test_semver_release_process.py`, observed failing first.

## Regression Strategy

`scripts/tests/test_ci_workflow_invariants.py`, `test_workflow_concurrency.py`, docs guards.

## Playwright Strategy

No UI change; Playwright not affected.

## Deployment Strategy

Effective on next push to main once the secret exists.

## Rollback Strategy

Revert the commit.
