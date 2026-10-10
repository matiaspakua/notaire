> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#1452 merged (`7b338ec2`). Active OpenSpec `docs-1445-archive-pages-umbrella` is Done.

## Goals / Non-Goals

**Goals:** Move the shipped change into `archive/YYYY-MM-DD-*`.

**Non-Goals:** Owner decisions; Pages copy edits.

## Decisions

1. Keep #1445 open — PR uses only `Refs` + full URL.
2. Do not merge until GitHub Pages has redeployed #1451/#1452 tip (avoid cancelling main CI again before Deploy).

## Riesgos / Trade-offs

- [Keyword auto-close] → never place close/fix/resolve next to `#1445`.
- [Deploy cancellation] → merging mid-CI can cancel the CI that gates Deploy GitHub Page.

## Testing Strategy

| AC | Test |
|----|------|
| validate-sdlc-plan green | `validate-sdlc-plan.sh` |

## Regression Strategy

- Existing ADR/Pages guards stay green.

## Observability / Security / Data

- None.

## Playwright Strategy

- n/a

## Deployment Strategy

- Docs-only merge after Pages shows #1445 (or after main CI+Deploy for current tip succeeds).

## Rollback Strategy

- Revert PR.
