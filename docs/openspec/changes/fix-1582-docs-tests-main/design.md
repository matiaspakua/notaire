# Design

## Context

Two concurrent PRs each added a `### Changed` section.

## Goals / Non-Goals

Goals: docs/tests green on main. Non-goals: changing the docs checks.

## Decisions

Merge the duplicate section instead of relaxing the check.

## Riesgos / Trade-offs

None; text only.

## Testing Strategy

The existing docs/tests failed before (red) and pass after.

## Regression Strategy

quick.sh.

## Playwright Strategy

N/A (docs only).

## Deployment Strategy

Docs only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
