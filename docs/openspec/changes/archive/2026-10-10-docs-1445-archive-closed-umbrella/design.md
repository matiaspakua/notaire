> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Closed issue #1197 broke `validate-sdlc-plan.sh` for the still-active OpenSpec change.

## Goals / Non-Goals

**Goals:** Green `workspace/verify` / validate-sdlc-plan.
**Non-Goals:** Owner decisions; deleting permanent ADR-024 / REPO-SPLIT-PLAN.

## Decisions

1. Archive topology OpenSpec change; permanent docs remain SSOT.
2. Keep live Owner work on issue 1445.

## Riesgos / Trade-offs

- [Loss of active OpenSpec checklist] → Permanent ADR/plan already on main; archive preserves history.

## Testing Strategy

| AC | Test |
|----|------|
| validate-sdlc-plan / workspace verify green | shell evidence in PR |

## Regression Strategy

- Existing unit guards stay green.

## Observability / Security / Data

- None.

## Playwright Strategy

- n/a

## Deployment Strategy

- Docs-only.

## Rollback Strategy

- Revert PR; restore OpenSpec trees from git.
