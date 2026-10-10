> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Architecture already names #1445 (ADR-024) and #1438 (ADR-022). LICENSE #1226
is missing from the public surface. Overview does not list the pack.

## Goals / Non-Goals

**Goals:** One Architecture section listing #1445, #1438, #1226 with GitHub issue
links; pin with unit guard; archive prior hygiene OpenSpec.

**Non-Goals:** Choosing options; adding LICENSE file; closing issues.

## Decisions

1. Use issue URLs (not only `#N`) so the public site deep-links GitHub.
2. PR/commit footers: `Refs` + full #1445 URL only.

## Riesgos / Trade-offs

- [Keyword auto-close] → never place close/fix/resolve next to issue numbers.
- [Deploy cancel race] → prefer merge when main CI is idle.

## Testing Strategy

| AC | Test |
|----|------|
| Architecture page cites #1445, #1438, #1226 | `test_adr024_owner_tracker.py` |
| validate-sdlc-plan green | `validate-sdlc-plan.sh` |

## Regression Strategy

- Existing ADR-022 / #1445 Pages guards stay green.

## Observability / Security / Data

- None.

## Playwright Strategy

- n/a (unit guard + Pages deploy).

## Deployment Strategy

- Merge docs/Pages PR; wait for CI → Deploy GitHub Page.

## Rollback Strategy

- Revert PR.
