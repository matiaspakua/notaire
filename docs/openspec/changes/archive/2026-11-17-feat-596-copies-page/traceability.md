# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #596 | open |
| Use Case | CU70 — Copies | exists |
| Specification | `docs/openspec/changes/feat-596-copies-page/` | drafted |
| Branch | `cursor/596-copies-page-47dc` | created |
| Tasks | `tasks.md` | pending |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Default request returns content and totalElements | `SimpleControllersTest` | passing |
| The created copy is inside content | `copies/02-list.yml` | pending preflight |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/openapi/openapi.yaml` | yes | pending |
| `frontend/src/types/api.generated.ts` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate after fill |
| 2 | Failing test | yes | findPage missing |
| 3 | Suite green | no | pending preflight |
| 4 | Review | no | pending |
| 5 | Owner merge | no | #596 stays open |

## Exceptions

No exception. Label writes return 403.
