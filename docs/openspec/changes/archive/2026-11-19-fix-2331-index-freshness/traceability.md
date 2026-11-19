# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2331 | open |
| Use Case | CU76 | exists |
| Branch | `cursor/repolens-index-freshness-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Stale graph misses the new symbol | `test_a_query_refreshes_when_head_moved_past_the_index` | passing |
| Query refreshes and finds it | same | passing |
| Second query does not warn | same | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| CHANGELOG | yes | this change |
| AGENTS.md | yes | this change |
| factory-kpis.json | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing test | yes | no results, empty stderr |
| 3 | Suite | no | pending verify.sh and preflight |

## Exceptions

No exception. No open issue covered this. #2331 is the tracker. The fixture measurement is 0 hits in 0.0019s, then a 0.01s refresh and a hit in a 0.0164s query. A full Notaire incremental refresh was not timed.
