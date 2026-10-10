> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#1477 closed #1462 while `feat-1462-otel-metrics-exporters` stayed active, failing
`validate-sdlc-plan.sh`. Prior archive Gate 1 and #1480 Gate 1 also remain active.

## Goals / Non-Goals

**Goals:** Restore green `validate-sdlc-plan.sh`; archive shipped Gate 1 trees.

**Non-Goals:** Owner ADR-024 / LICENSE / deprecated/ decisions.

## Decisions

1. Keep #1445 open — Refs + full URL only; never Closes/Fixes next to `#1445`.
2. Bundle three archive moves in one docs-only PR.

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
