# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2010 | open |
| Use Case | CU78 | exists |
| Branch | `cursor/2010-api-quota-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Burst then next window | `ApiRequestQuotaTest` | passing |
| Filter 429 | `ApiRequestQuotaTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| THREAT-MODEL-SYSTEM.md | yes | this change |
| THREAT-MODEL-AUTH-AUDIT.md | yes | this change |
| system.stride.yml | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing test | yes | missing class |
| 3 | Suite | no | pending preflight |

## Exceptions

No exception. #2010 stays open because the counter is not shared across API instances.
