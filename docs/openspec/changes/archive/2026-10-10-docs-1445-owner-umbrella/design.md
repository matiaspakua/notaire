> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Keyword parsing closed prior umbrellas. Live tracker #1445.

## Goals / Non-Goals

**Goals:** Point docs at #1445; guard drift.

**Non-Goals:** Owner decision content; reopening closed issues.

## Decisions

1. Phrase carefully — never place close/fix/resolve next to .
2. Prefer  + full issue URL in PR footers.

## Riesgos / Trade-offs

- [Repeat closure] → PR body uses only  and full URL; no "do not close" phrasing.

## Testing Strategy

| AC | Test |
|----|------|
| Docs cite #1445 |  |

## Regression Strategy

- Existing Pages/business guards stay green.

## Observability / Security / Data

- None.

## Playwright Strategy

- n/a docs only.

## Deployment Strategy

- Docs-only merge; Pages redeploy if CI on main succeeds (no github-page path required).

## Rollback Strategy

- Revert PR. Issue #1445 remains the human tracker.
