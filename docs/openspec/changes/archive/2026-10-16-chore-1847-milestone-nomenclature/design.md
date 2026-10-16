# Design — version-first milestone nomenclature

## Context

`milestone_policy.py` holds titles, windows and routing; `apply_milestone_policy.py`
applies them with an owner token; `product_release.py` writes the train into
the release record, the SBOM and the Pages history.

## Goals / Non-Goals

**Goals:** one naming scheme for every milestone; prefix-based `train_for()`;
an apply run that can rename, merge, create closed milestones and sync
descriptions idempotently.

**Non-Goals:** changing the board views or labels; deleting milestones.

## Decisions

- **H8 is one merged milestone, not H8a/b/c.** The buckets were priority
  slices of the same period; priority is already a label, and one milestone
  per stage keeps the history linear. #12 is renamed; issues on #13/#14 move
  to it, and those two stay as empty closed milestones (nothing is deleted).
- **Cut windows by tag instant (UTC).** v0.1 = 2026-10-01 → `notaire-v0.1.2`;
  v0.2 = after it → `v0.2.3`. Only unmilestoned, Q4-cohort and delivery-train
  issues move; curated stages keep theirs.
- **After the last cut**, an unmilestoned closed issue goes to the current train (v0.3).
- **Catalog first** in `classify_open`: anchors without work labels → Catalog.
- **Closed milestones are created open**, issues moved, then closed in the same run.
- `RELEASE_TRAINS` is duplicated in `product_release.py` (no import across
  packages), guarded by a test that it equals `SHIPPED + SCHEDULED`.

## Riesgos / Trade-offs

- The committed snapshots are re-keyed, not regenerated; the Owner's apply run refreshes counts.
- Issues closed just after a tag but before UTC midnight are judged by timestamp, so they are correct.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| Open trains are version first | unit | `NomenclatureTests.test_open_trains_are_version_first_and_ordered` |
| History stages H0–H8 | unit | `test_history_stages_run_from_the_swing_monolith_to_pre_release_hardening` |
| Continuous milestones have no due date | unit | `test_backlog_and_catalog_are_continuous_without_due_date`, `test_payload_closes_history_and_shipped_and_leaves_continuous_undated` |
| Description template | unit | `test_descriptions_follow_the_template` |
| Buckets merge into H8 | unit | `test_old_titles_rename_to_the_new_nomenclature`, `test_merged_bucket_issues_move_to_h8` |
| Cut windows | unit | `ShippedTrainTests.*`, `test_closed_cohort_issue_in_a_cut_window_moves_to_the_shipped_train` |
| Catalog anchors | unit | `test_catalog_anchors_live_on_the_catalog_milestone`, `test_open_catalog_anchor_targets_the_catalog` |
| jpa → v0.3 | unit | `test_jpa_retirement_moved_into_the_hexagonal_train` |
| Prefix train_for | unit | `test_train_is_the_milestone_with_the_version_prefix`, `test_unmapped_minor_fails_closed`, `test_release_trains_match_the_milestone_policy` |

## Regression Strategy

All existing `test_milestone_policy.py` and `test_product_release.py` cases
still run; the RNF-to-train cases were replaced because RNF anchors now live
on Catalog (Owner decision). `product_release.py --check` stays green.

## Playwright Strategy

n/a — no product UI surface; the GitHub Pages roadmap is built by `next build` in CI.

## Deployment Strategy

- Flyway migration required: no
- Feature flag: no
- After merge, the Owner runs `/workspace/notaire-milestones-apply.sh`
  (wraps `apply_milestone_policy.py --dry-run` then `--apply`).

## Rollback Strategy

- Revert safe: yes, code and docs only.
- Database rollback: none needed. GitHub titles can be renamed back by a reverted apply run.

## Migration Plan

1. Merge this PR. 2. Owner runs the apply script (dry run, then apply). 3. `--check` reports zero drift.

## Open Questions

None.
