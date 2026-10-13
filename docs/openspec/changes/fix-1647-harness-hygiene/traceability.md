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
| Issue | #1647 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-1647-harness-hygiene/` | archived in this PR |
| Branch | `fix/1647_harness-hygiene` | created |
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
| opencode config is portable | `OpencodePortabilityTest` | passing |
| Never-pushed branch is listed | `UnpushedWorkWarningTest.test_session_start_lists_branches_never_pushed` | passing |
| Workflow rule names the verification | `PushVerificationRuleTest.test_workflow_rule_verifies_the_push_landed` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| <!-- path --> | <!-- yes/no --> |  |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1647 |
| 2 | Failing tests written, test cases designed | yes | 4 FAIL before |
| 3 | Suite green, coverage held, docs updated | yes | tests OK; hook run on the real checkout |
| 4 | CI green, review approved, no conflicts | | pending |
| 5 | Deployed, smoke test passed, Issue closed | | pending |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None.
