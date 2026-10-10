> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Product merges closed #1408 / #1367 / #1466 while OpenSpec trees stayed active.
#1470 Gate 1 also remains active.

## Goals / Non-Goals

**Goals:** Restore green `validate-sdlc-plan.sh`.

**Non-Goals:** Owner ADR-024 / LICENSE decisions.

## Decisions

1. Keep #1445 open — Refs + full URL only.
2. Bundle four archive moves in one docs-only PR.

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
