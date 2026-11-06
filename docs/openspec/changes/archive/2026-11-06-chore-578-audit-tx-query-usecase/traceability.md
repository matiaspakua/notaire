# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #578 | in-progress |
| Use Case | CU76 | exists |
| Specification | `docs/openspec/changes/chore-578-audit-tx-query-usecase/` | passed |
| Branch | `chore/578_audit-tx-query-usecase` | created |
| Tasks | `tasks.md` | in progress |
| Commits | passing | pending |
| Pull Request | passing | pending |
| CI run | passing | pending |
| Merge commit | passing | pending |
| Release / tag | passing | pending |
| Smoke test | passing | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| No @Transactional under adapter.in.web | AuditRecordControllerNoWebTransactionalTest | passing |
| getById delegates to query use case | AuditRecordControllerTest | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | no | passing |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #578, this folder |
| 2 | Failing tests written, test cases designed | passing | pending |
| 3 | Suite green, coverage held, docs updated | passing | pending |
| 4 | CI green, review approved, no conflicts | passing | pending |
| 5 | Deployed, smoke test passed, Issue closed | passing | pending |

## Exceptions

None.
