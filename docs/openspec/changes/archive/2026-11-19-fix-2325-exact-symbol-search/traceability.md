# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2325 | open |
| Use Case | CU76 | exists |
| Branch | `cursor/repolens-exact-symbol-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Exact symbol skips embed_query | `test_an_exact_symbol_skips_the_embedding_query_and_lists_its_caller` | passing |
| Caller with empty full-text | same | passing |
| adoption n/a | `test_gate_line_does_not_report_zero_adoption_when_there_are_no_sessions` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| RepoLens changelog | yes | this change |
| factory-kpis.json | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing tests | yes | embed count 1; adoption 0% |
| 3 | Suite | no | pending verify.sh and preflight |

## Exceptions

No exception. #2325 was opened because #2307 is closed and this change needs an open issue. The Pages UI was not edited.
