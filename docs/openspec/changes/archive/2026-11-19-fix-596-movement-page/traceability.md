# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #596 | open |
| Use Case | CU74 | exists |
| Branch | `cursor/596-movement-page-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Sort whitelist | `MovementPageSortTest` | passing |
| Pageable controller | `SimpleControllersTest` | passing |
| Page body | `PreviouslyBrokenEndpointsIntegrationTest` | pending full suite |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| OpenAPI | yes | this change |
| factory learning log | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing test | yes | missing MovementPageSort |
| 3 | Suite | no | pending preflight |

## Exceptions

No exception. #596 stays open. Testimonies, properties, and items stay unpaged because those lists also feed a screen that needs the full set.
