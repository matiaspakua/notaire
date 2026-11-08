# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1847 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-1847-milestone-nomenclature/` | Gate 1 |
| Branch | `chore/1847_milestone_nomenclature` | from `origin/main` |
| Tasks | `tasks.md` | groups 1–9 done; 10–12 at PR/merge |
| Commits | see PR | recorded in PR |
| Pull Request | see PR for #1847 | pending |
| CI run | PR checks | pending |
| Merge commit | Owner merge | pending |
| Release / tag | next cut | pending |
| Smoke test | Owner apply run + `apply_milestone_policy.py --check` | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| The current trains | `test_open_trains_are_version_first_and_ordered` | passing |
| Priority buckets merge into H8 | `test_old_titles_rename_to_the_new_nomenclature`, `test_merged_bucket_issues_move_to_h8` | passing |
| Closed between the v0.1 and v0.2.3 tags | `test_closed_cohort_issue_in_a_cut_window_moves_to_the_shipped_train`, `ShippedTrainTests` | passing |
| Curated history is kept | `test_curated_history_and_shipped_trains_are_kept` | passing |
| A use-case anchor | `test_open_catalog_anchor_targets_the_catalog`, `test_payload_closes_history_and_shipped_and_leaves_continuous_undated` | passing |
| Template check | `test_descriptions_follow_the_template` | passing |
| Patches share their minor's train | `test_train_is_the_milestone_with_the_version_prefix` | passing |
| A minor with no train fails | `test_unmapped_minor_fails_closed` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/305-project-management/MILESTONE-NOMENCLATURE.md` | yes | this PR |
| `docs/300-development/305-project-management/MILESTONES.md` | yes | this PR |
| `docs/300-development/305-project-management/RELEASES.md` | yes | this PR |
| `docs/300-development/305-project-management/README.md` | yes | this PR |
| `docs/300-development/305-project-management/ROADMAP.md` | yes | this PR |
| `CONSTITUTION.md` | yes | this PR |
| `docs/200-architecture/201-SAD/sad.md` | yes | this PR |
| `docs/200-architecture/202-ADR/ADR-028-release-and-version-identity.md` | yes | this PR |
| `docs/300-development/SBOM.md` | yes | this PR |
| `docs/github/README.md` | yes | this PR |
| `CHANGELOG.md` | yes | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1847, this change |
| 2 | Failing tests written, test cases designed | yes | test commits before implementation |
| 3 | Suite green, coverage held, docs updated | yes | quick.sh |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Owner apply run |

## Exceptions

None.
