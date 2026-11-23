# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2414 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-2414-stack-lock/` | seeded |
| Branch | `chore/2414_stack_lock` | created |
| Tasks | `tasks.md` | 8/8 complete |
| Commits | ecffa26c, e4729869 | complete |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Acquire then release lock | `test_stack_lock.py` | passing |
| Second acquire waits and times out | `test_stack_lock.py` | passing |
| Stale lock (dead PID) is taken over | `test_stack_lock.py` | passing |
| Two concurrent acquirers, one wins | `test_stack_lock.py` | passing |
| preflight.sh calls stack_lock.sh acquire | `test_stack_lock.py` | passing |
| Smoke test always reaches docker compose down | `test_stack_lock.py` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `testing/e2e/README.md` | yes | e4729869 |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | no | pending |
| 2 | Failing tests written, test cases designed | no | pending |
| 3 | Suite green, coverage held, docs updated | no | pending |
| 4 | CI green, review approved, no conflicts | no | pending |
| 5 | Deployed, smoke test passed, Issue closed | no | pending |

## Exceptions

None.
