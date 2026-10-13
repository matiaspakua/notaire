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
| Issue | #1624 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/docs-1624-foreman-merge-authority/` | drafted |
| Branch | `docs/1624_foreman-merge-authority` | created |
| Tasks | `tasks.md` | in progress |
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
| Harness runs the heavy-CI gate before merging | `MergeAuthorityTest.test_local_foreman_runs_heavy_ci_gate_before_merging` | passing |
| Governance docs name the rule | `test_rule_docs_name_the_foreman_merge_rule`, `test_no_doc_keeps_the_owner_only_merge_rule` | passing |
| Owner-only merges stay explicit | `test_constitution_keeps_owner_only_merges_for_amendments_and_exceptions` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| <!-- path --> | <!-- yes/no --> |  |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1624 Owner decision (b); `validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | yes | `test_merge_authority.py` 8 FAIL + 1 ERROR before |
| 3 | Suite green, coverage held, docs updated | yes | process self-tests OK |
| 4 | CI green, review approved, no conflicts | | pending — **Owner merges** (Constitution amendment) |
| 5 | Deployed, smoke test passed, Issue closed | | pending |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None.
