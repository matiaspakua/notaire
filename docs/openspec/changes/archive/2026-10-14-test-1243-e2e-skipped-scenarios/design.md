> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The existing guard already fails when a skip does not cite an issue and when counts drift from the inventory; this change only shrinks the inventory because the gaps closed.

## Goals / Non-Goals

**Goals:** no stale skip claims; fewer hidden use cases.
**Non-Goals:** new product features.

## Decisions

1. **Verify each claim against the frontend before deciding.** Search inputs and the template action exist, so those become tests. 2. **Keep the three real gaps skipped** instead of deleting them, so the use cases stay visibly uncovered.

## Riesgos / Trade-offs

- [A new test is flaky on shared data] → each search test seeds nothing and asserts on the page's own list after searching for a value read from the page, or asserts the empty-state; verified by repeated runs.

## Testing Strategy

python3 testing/tests/test_e2e_reliability.py plus npx playwright test TS-0020 against the running stack.

## Regression Strategy

python3 -m unittest discover -s testing/tests; cd testing/e2e && npx playwright test TS-0020

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
