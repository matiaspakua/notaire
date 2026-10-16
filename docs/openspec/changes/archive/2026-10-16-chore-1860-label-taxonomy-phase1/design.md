# Design — label taxonomy phase 1

## Context

Label names are literals in the claim script, the coherence gate, the lane policy,
the exception checks and the project view filters.

## Goals / Non-Goals

**Goals:** every reader gives the same answer for a legacy label and its new name.
**Non-Goals:** performing the rename; removing legacy names (phase 2).

## Decisions

- One table (`label_taxonomy.RENAMES`), and `expand()` adds both directions, so
  existing checks on legacy names keep working unchanged.
- View filters list both names separated by commas (OR in Project filters), so
  they are right before and after the rename.
- The claim writes `status:in-progress` only when that label exists, so it never
  recreates a legacy label after the rename, nor a new one before it.

## Riesgos / Trade-offs

- Requiring readiness stops agents from claiming until the Owner labels work ready
  (2 open issues are `ready-for-dev` today).
- Without the Priority field, views cannot sort by priority; priority is a label filter.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| Claim scenarios | unit | `test_label_taxonomy.ClaimRules` |
| Coherence R6–R8, new WIP name | unit | `test_label_taxonomy.CoherenceRules` |
| Exception label | unit | `ExpandTests`, script grep |
| Views | unit | `test_milestone_policy.test_nine_views_from_the_label_taxonomy`, `test_views_do_not_use_deleted_board_fields` |

## Regression Strategy

All workspace suites (quick.sh); `test_claim_issue.py` fixture now carries `ready-for-dev`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

- Flyway migration required: no
- Feature flag: no
- After merge the Owner runs `notaire-labels-apply.sh --apply`, then builds the views from the guide.

## Rollback Strategy

- Revert safe: yes (old names are still accepted)
- Database rollback: none needed

## Migration Plan

1. Merge. 2. Owner runs the label script. 3. Views per guide. 4. Phase 2 PR drops legacy names.

## Open Questions

None.
