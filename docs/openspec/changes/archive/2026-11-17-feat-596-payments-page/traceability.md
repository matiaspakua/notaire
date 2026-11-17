# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #596 | open |
| Use Case | CU47 — Query payment status | exists |
| Specification | `docs/openspec/changes/feat-596-payments-page/` | drafted |
| Branch | `cursor/596-payments-page-47dc` | created |
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
| Default request returns content and totalElements | `PaymentControllerTest` | passing |
| The screen requests page, size and sort | `payments-page-actions.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/openapi/openapi.yaml` | yes | pending |
| `frontend/src/types/api.generated.ts` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate after fill |
| 2 | Failing test | yes | findPage missing, then 500 without the Pageable resolver |
| 3 | Suite green | no | pending preflight |
| 4 | Review | no | pending |
| 5 | Owner merge | no | #596 stays open |

## Exceptions

No exception. Label writes return 403.
