> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#1368 and #1354 CLOSED after #1433 / #1428; OpenSpec trees still active.
#1455 Gate 1 (`docs-1445-archive-owner-decisions`) shipped and should leave the active set.

## Goals / Non-Goals

**Goals:** Archive the shipped / closed-issue changes so validate-sdlc-plan is green.

**Non-Goals:** Frontend code changes; Owner ADR-024 / LICENSE decisions.

## Decisions

1. Keep #1445 open — Refs + full URL only.
2. Bundle the three archive moves in one docs-only PR.

## Riesgos / Trade-offs

- [Keyword auto-close] → never place close/fix/resolve next to `#1445`.

## Testing Strategy

| AC | Test |
|----|------|
| validate-sdlc-plan green | `validate-sdlc-plan.sh` |

## Regression Strategy

- n/a

## Observability / Security / Data

- None.

## Playwright Strategy

- n/a

## Deployment Strategy

- Docs-only merge.

## Rollback Strategy

- Revert PR.
