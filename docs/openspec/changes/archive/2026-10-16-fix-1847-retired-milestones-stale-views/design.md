# Design — retired milestones and stale views

## Context

`ensure_milestones()` renames a source only while its target is missing; a second
source for the same target (two buckets → H8) is left behind once emptied.

## Goals / Non-Goals

**Goals:** no empty retired milestone; `--check` sees leftovers. **Non-Goals:** deleting views from the script (Owner script / UI).

## Decisions

- Delete only when the milestone holds zero issues; otherwise report it, as the move step runs first.
- An old view name with a VIEW_ALIASES entry is not stale while its new view is missing, because `configure_views()` renames it.

## Riesgos / Trade-offs

- Milestone delete is irreversible; restricted to empty rename sources.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| Empty bucket merged into H8 | unit | `ConsolidationTests.test_merged_bucket_is_retired_once_its_target_exists` |
| Bucket before its target exists | unit | `ConsolidationTests.test_bucket_is_not_retired_before_its_target_exists` |
| Pre-taxonomy view | unit | `ConsolidationTests.test_views_outside_the_contract_are_stale`, `test_alias_is_not_stale_while_its_new_view_is_missing` |

## Regression Strategy

All `test_milestone_policy.py` cases; `--dry-run` against live GitHub (read-only).

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

1. Merge. 2. Owner runs `notaire-consolidate.sh --apply`. 3. `--check` reports 0 mismatches.

## Open Questions

None.
