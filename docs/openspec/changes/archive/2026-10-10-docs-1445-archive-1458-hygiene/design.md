> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#1457 and #1458 merged; their Gate 1 trees remain active.

## Goals / Non-Goals

**Goals:** Archive both shipped Gate 1 changes.

**Non-Goals:** Owner ADR-024 / LICENSE decisions.

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
