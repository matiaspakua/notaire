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
| Branch | `cursor/580-procedure-folder-mapper-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Fields copy | `ProcedureFolderLegacyMapperTest` | passing |
| Null links | `ProcedureFolderLegacyMapperTest` | passing |
| No getDto | `ProcedureFolderLegacyMapperTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| Product docs | no | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing test | yes | missing mapper |
| 3 | Suite | no | pending preflight |

## Exceptions

No exception. Locate used `rg` only, per the routing rule for a known symbol. This is not a sampled A/B issue.
