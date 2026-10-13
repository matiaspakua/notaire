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
| Issue | #1623 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-1623-gate5-archive-in-pr/` | archived in this PR |
| Branch | `fix/1623_gate5-archive-in-pr` | created |
| Tasks | `tasks.md` | groups 1–9 complete |
| Commits |  | pending |
| Pull Request |  | pending |
| CI run |  | pending |
| Merge commit |  | pending |
| Release / tag |  | pending |
| Smoke test |  | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Touched change left active fails | `ArchiveInPullRequestTest.test_touched_change_left_active_fails` | passing |
| Change archived in the PR passes | `test_change_archived_in_pr_passes` | passing |
| Multi-PR change may stay active | `test_multi_pr_change_may_stay_active` | passing |
| Leftover only warns on a pull request | `InventoryBlastRadiusTest.test_leftover_only_warns_on_a_pull_request` | passing |
| Leftover fails in inventory mode | `test_leftover_fails_in_inventory_mode` | passing |
| MODIFIED on a missing spec fails | `DeltaOpsTest.test_modified_on_a_missing_spec_fails` | passing |
| ADDED on an existing requirement fails | `DeltaOpsTest.test_added_requirement_that_already_exists_fails` | passing |
| Main health runs on push, schedule and dispatch | `SdlcProcessOnMainTest` (3 tests) | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| <!-- path --> | <!-- yes/no --> |  |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1623, #1622 |
| 2 | Failing tests written, test cases designed | yes | see tasks 3.4 |
| 3 | Suite green, coverage held, docs updated | yes | 8 self-test suites OK; 182/182 specs |
| 4 | CI green, review approved, no conflicts | | pending — **Owner merges** (Constitution amendment) |
| 5 | Deployed, smoke test passed, Issue closed | | pending — first Main Health run on main is the smoke |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None.
