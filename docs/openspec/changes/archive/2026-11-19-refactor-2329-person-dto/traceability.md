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
| Branch | `cursor/2329-person-mapper-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Client fields | `PersonLegacyMapperTest` | passing |
| Non-client skips nationality | `PersonLegacyMapperTest` | passing |
| No setAttributes | `PersonLegacyMapperTest` | passing |

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

No exception. Locate used `rg` only, because the symbol was known. The only production caller was `User.setAttributes`. #2329 stays open because DeedManagement, Procedure, and User remain on the ratchet.
