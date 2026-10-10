> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#1368 CLOSED; OpenSpec tree still active after merge.

## Goals / Non-Goals

**Goals:** Archive the shipped change.

**Non-Goals:** Frontend motion code changes.

## Decisions

1. Keep #1445 open — Refs + full URL only.

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
