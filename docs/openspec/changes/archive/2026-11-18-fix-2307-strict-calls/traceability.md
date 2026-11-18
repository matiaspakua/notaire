# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2307 | open |
| Use Case | CU76 | exists |
| Branch | `cursor/2307-strict-calls-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Ambiguous getDto is not guessed; unique name still links | `test_an_ambiguous_same_directory_call_is_not_guessed` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| CHANGELOG 0.4.2 | yes | this change |
| factory-loop-comparison.md | yes | this change |
| factory-learning-log.md | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing test | yes | Folio.getDto was linked |
| 3 | verify.sh | yes | 70 passed |

## Exceptions

No exception. The live index is not rebuilt in this change.
