> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#1454 merged (`038c93a1`). Active OpenSpec `docs-1445-pages-owner-decisions` is Done.

## Goals / Non-Goals

**Goals:** Archive the shipped change.

**Non-Goals:** Owner decisions; Pages edits.

## Decisions

1. Keep #1445 open — `Refs` + full URL only.
2. Merge only after Pages shows the Owner decisions pack (avoid cancelling tip CI/Deploy).

## Riesgos / Trade-offs

- [Keyword auto-close] → never place close/fix/resolve next to `#1445`.
- [Deploy cancel] → hold merge until Deploy for the Pages tip has succeeded.

## Testing Strategy

| AC | Test |
|----|------|
| validate-sdlc-plan green | `validate-sdlc-plan.sh` |

## Regression Strategy

- Existing guards stay green.

## Observability / Security / Data

- None.

## Playwright Strategy

- n/a

## Deployment Strategy

- Docs-only merge after Pages pack is live.

## Rollback Strategy

- Revert PR.
