> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Failing run screenshot: /testimonies empty, placeholder row counted; DataTable renders the empty state as one tr with td colSpan.

## Goals / Non-Goals

**Goals:** The check is correct on empty and populated lists.
**Non-Goals:** Making every E2E test independent of database state.

## Decisions

1. **Exclude the placeholder structurally.** Rows are counted as `tbody tr` without a `td[colspan]` cell, not by text, so a translated empty message does not matter.
2. **Skip, do not pass, an empty list.** The check has nothing to verify; the empty state is covered elsewhere.

## Riesgos / Trade-offs

- [Another table uses colspan rows for groups] -> none exists today; the check is per table with a visible badge column.

## Testing Strategy

`testing/e2e/tests/TS-0124-status-badges.spec.ts` on a fresh stack, empty and populated lists.

## Regression Strategy

Full Playwright suite on a fresh stack: `cd testing/e2e && npx playwright test`.

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
