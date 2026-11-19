# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2326 | open |
| Use Case | CU76 | exists |
| Branch | `cursor/580-testimony-mapper-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Fields copy | `TestimonyLegacyMapperTest` | passing |
| Apply | `TestimonyLegacyMapperTest` | passing |
| No getDto or setAttributes | `TestimonyLegacyMapperTest` | passing |

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

No exception. This is the sampled A/B issue. `rg` took 0.004s. `repolens search Testimony.getDto` took 0.680s and returned no results. `repolens impact` took 0.205s and printed `node not found`. The caller list came from `git grep` on `origin/main` (0.015s).
