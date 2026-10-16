# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. Rows below Tasks stay `pending` until the step happens.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1860 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-1860-label-taxonomy-phase1/` | Gate 1 |
| Branch | `chore/1860_label_taxonomy_phase1` | from `origin/main` |
| Tasks | `tasks.md` | groups 1–9 done; 10–12 at PR/merge |
| Commits | see PR | recorded in PR |
| Pull Request | see PR for #1860 | pending |
| CI run | PR checks | pending |
| Merge commit | Owner merge | pending |
| Release / tag | next cut | pending |
| Smoke test | Owner label script + coherence check | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Unready issue is refused | `ClaimRules.test_claim_requires_ready` | passing |
| Either ready name is accepted | `ClaimRules.test_claim_accepts_old_or_new_ready` | passing |
| New hold names refuse | `ClaimRules.test_claim_refuses_new_blocked_names` | passing |
| Written WIP label follows the repository | `ClaimRules.test_wip_label_follows_the_repo` | passing |
| New WIP name satisfies In Progress | `CoherenceRules.test_new_wip_name_satisfies_r2` | passing |
| In review without an open PR | `CoherenceRules.test_r6_in_review_needs_open_pr` | passing |
| Ready and blocked together | `CoherenceRules.test_r7_ready_and_blocked_exclusive` | passing |
| Ready issue with two types | `CoherenceRules.test_r8_ready_issue_has_at_most_one_type` | passing |
| Taxonomy exception label | `ExpandTests.test_new_name_brings_its_legacy_name` | passing |
| Agent queue lists only ready, unheld Todo work | `test_nine_views_from_the_label_taxonomy` | passing |
| Filters survive the rename | `test_full_lifecycle_and_delivery_filters_have_distinct_scope` | passing |
| No view uses a deleted field | `test_views_do_not_use_deleted_board_fields` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/305-project-management/LABELS.md` | yes | this PR |
| `docs/300-development/305-project-management/PARALLEL-AGENTS.md` | yes | this PR |
| `docs/github/README.md` | yes | this PR |
| `CHANGELOG.md` | yes | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1860, this change |
| 2 | Failing tests written, test cases designed | yes | test commit before implementation |
| 3 | Suite green, coverage held, docs updated | yes | quick.sh |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Owner script, phase 2 |

## Exceptions

None.
