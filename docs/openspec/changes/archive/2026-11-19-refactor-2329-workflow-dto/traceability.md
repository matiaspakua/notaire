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
| Branch | `cursor/2329-workflow-mapper-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Definition fields | `WorkflowLegacyMapperTest` | passing |
| Node links | `WorkflowLegacyMapperTest` | passing |
| Transition endpoints | `WorkflowLegacyMapperTest` | passing |
| No entity toDto | `WorkflowLegacyMapperTest` | passing |

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

No exception. Locate used `rg` only, because the symbols were known. This is not a sampled locate pair. #2329 stays open because DeedManagement, Procedure, Person, and User remain on the ratchet.
