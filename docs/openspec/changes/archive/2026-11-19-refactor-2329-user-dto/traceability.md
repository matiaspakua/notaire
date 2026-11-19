# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2329 | open |
| Use Case | CU76 | exists |
| Branch | `cursor/2329-user-mapper-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Account and person | `UserLegacyMapperTest` | passing |
| Null version | `UserLegacyMapperTest` | passing |
| No setAttributes | `UserLegacyMapperTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| Product docs | no | n/a |
| factory-kpis.json | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing test | yes | missing mapper |
| 3 | Suite | no | pending preflight |

## Exceptions

No exception. This is the sampled locate. `git grep` found only `User.java`. The Notaire first-query refresh time is in `factory-kpis.json`. #2329 stays open because DeedManagement and Procedure remain on the ratchet.
