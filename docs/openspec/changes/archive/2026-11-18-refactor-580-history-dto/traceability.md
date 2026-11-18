# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #580 | open |
| Use Case | CU76 | exists |
| Branch | `cursor/580-history-mapper-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| History has no getDto | `HistoryDtoRemovalTest` | passing |
| Summary mapping unchanged | `HistoryMapperTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| factory-loop-comparison.md | yes | this change |
| Product docs and Pages | no | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing test | yes | getDto still declared |
| 3 | Suite | no | pending preflight |

## Exceptions

No exception. The live history API already uses `HistoryMapper`.
