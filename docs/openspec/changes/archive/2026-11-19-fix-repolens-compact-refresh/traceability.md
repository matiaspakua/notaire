# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2336 | open |
| Use Case | CU76 | exists |
| Branch | `cursor/repolens-compact-refresh-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Query does not wait for refresh | `test_a_query_does_not_wait_for_the_refresh_after_head_moves` | passing |
| Default search is at most 4 detailed hits | `test_default_search_keeps_four_detailed_hits` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| AGENTS.md | yes | this change |
| RepoLens CHANGELOG | yes | this change |
| factory-kpis.json | yes | this change |
| factory-learning-log.md | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing test | yes | stderr still said refreshed in 0.01s |
| 3 | Suite | no | pending preflight |

## Exceptions

No exception. #2336 stays open. This is the payload and refresh slice, not the whole issue. The Sonnet bench was not re-run.
