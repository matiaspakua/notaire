> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#1197 closed via keyword parse in #1442 squash body (`Closes packaging gap for #1197`). Live Owner
decisions still required; #1443 opened as replacement tracker.

## Goals / Non-Goals

**Goals:** Point ADR-024 and REPO-SPLIT-PLAN at #1443; guard against silent drift.

**Non-Goals:** Owner decision content; reopening #1197; deleting historical #1197 citations.

## Decisions

1. Keep #1197 in historical/critique text; Deciders + issue-map umbrella use #1443.
2. Unit guard pattern matches `test_adr022_owner_decision_pack.py`.

## Riesgos / Trade-offs

- [Another auto-close] → PR/commits use `Refs #1443` only; never `Closes` near `#1443` until Owner done.
- [Two umbrella issues] → Document #1197 closed erroneously; #1443 is authoritative for remaining work.

## Testing Strategy

| Acceptance Criterion | Test |
|----------------------|------|
| ADR-024 / plan cite #1443 | `workspace/tests/test_adr024_owner_tracker.py` |

## Regression Strategy

- `test_adr022_owner_decision_pack` stays green.

## Observability / Security / Data

- None.

## Playwright Strategy

- n/a (docs only).

## Deployment Strategy

- Docs-only; Pages may redeploy after main CI if paths touch github-page (they do not).

## Rollback Strategy

- Revert PR; #1443 issue remains as the human tracker regardless.
