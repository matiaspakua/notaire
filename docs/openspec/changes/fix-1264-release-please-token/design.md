# Design

## Context

Run log: `GitHub Actions is not permitted to create or approve pull requests`.

## Goals / Non-Goals

Goal: the workflow is ready for the Owner's secret. Non-goal: creating the secret or changing repo Actions settings (Owner action).

## Decisions

Fallback to `github.token` keeps the workflow valid before the secret exists; bootstrap at the #1043 commit, as the issue suggests.

## Riesgos / Trade-offs

Until the secret is created the workflow keeps failing; bootstrap-sha choice is the Owner's to confirm.

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
