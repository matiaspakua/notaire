# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2351 | open |
| Use Case | CU85 — Archivar gestión | exists |
| Specification | `docs/openspec/changes/fix-2351-archive-conflict/` | passed |
| Branch | `cursor/2351-archive-conflict-47dc` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| an unconfirmed archive names the folder number | ManagementArchiveDebtServiceTest | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| none beyond this change | n/a | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2351, this folder |
| 2 | Unit test | yes | ManagementArchiveDebtServiceTest |
| 3 | Suite green | pending | preflight |
| 4 | CI green | pending | pending |
| 5 | Issue closed | pending | the issue stays open |

## Exceptions

None.
