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
| Issue | #1646 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-1646-atomic-issue-claim/` | archived in this PR |
| Branch | `feat/1646_atomic-issue-claim` | created |
| Tasks | `tasks.md` | groups 1–10.1 complete |
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
| Uncontested claim wins and marks the board | `ClaimFlowTest.test_uncontested_claim_wins_and_marks_the_board` | passing |
| Losing a race withdraws and fails | `ClaimFlowTest.test_losing_a_race_withdraws_and_fails` | passing |
| Busy lane refuses before posting | `ClaimFlowTest.test_busy_lane_refuses_before_posting` | passing |
| Stale claim without open PR | `DecisionTest.test_stale_claim_without_open_pr` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| <!-- path --> | <!-- yes/no --> |  |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1646 |
| 2 | Failing tests written, test cases designed | yes | test_claim_issue.py ERROR before |
| 3 | Suite green, coverage held, docs updated | yes | 7 tests OK; live refusal smoke |
| 4 | CI green, review approved, no conflicts | | pending |
| 5 | Deployed, smoke test passed, Issue closed | | pending |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None.
