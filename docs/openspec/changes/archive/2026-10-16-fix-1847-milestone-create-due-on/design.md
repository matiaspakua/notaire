# Design — milestone create omits a null due date

## Context

`ensure_milestones()` posted `milestone_payload()` with state open. For
Backlog and Catalog `due_on` is None, and the run stopped there.

## Goals / Non-Goals

**Goals:** creates succeed for every milestone kind. **Non-Goals:** policy changes.

## Decisions

- Drop null values from the POST body only. A PATCH still sends `due_on: null`
  to clear a date, which the live Backlog shows works.

## Riesgos / Trade-offs

- None beyond the existing apply run; it is idempotent, so a re-run continues where the first stopped.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| Continuous create has no due_on | unit | `CreateBodyTests.test_continuous_milestone_is_created_without_a_due_on_key` |
| Closed kinds are created open with a date | unit | `CreateBodyTests.test_closed_milestones_are_created_open_with_their_due_date` |

## Regression Strategy

All `test_milestone_policy.py` cases run; `--check` against live GitHub (read-only) lists the 562 pending moves.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

- Flyway migration required: no
- Feature flag: no
- After merge the Owner re-runs `notaire-milestones-apply.sh --apply`.

## Rollback Strategy

- Revert safe: yes
- Database rollback: none needed

## Migration Plan

1. Merge. 2. Owner re-runs the apply script. 3. `--check` reports 0 mismatches.

## Open Questions

None.
