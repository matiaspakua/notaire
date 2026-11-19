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
| Branch | `cursor/2329-procedure-mapper-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Optional links empty | `ProcedureLegacyMapperTest` | passing |
| Present links | `ProcedureLegacyMapperTest` | passing |
| Entity branches | `ProcedureEntityTest` | passing |

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

No exception. Sampled locate after the #2334 merge: `git grep` 0.017s; first `repolens search Procedure.getDto` 7.718s with `index refreshed in 5.79s`, ranking `Procedure.getDto` then `DeedLegacyMapper.toDto` and `TestimonyLegacyMapper.copyDeed`. #2329 stays open because `DeedManagement#getDto` remains.
