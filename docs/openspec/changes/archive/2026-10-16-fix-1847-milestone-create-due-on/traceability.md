# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1847 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-1847-milestone-create-due-on/` | Gate 1 |
| Branch | `fix/1847_milestone_create_due_on` | from `origin/main` |
| Tasks | `tasks.md` | groups 1–9 done; 10–12 at PR/merge |
| Commits | see PR | recorded in PR |
| Pull Request | see PR for #1847 | pending |
| CI run | PR checks | pending |
| Merge commit | Owner merge | pending |
| Release / tag | next cut | pending |
| Smoke test | Owner apply run + `apply_milestone_policy.py --check` | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Continuous create has no due_on | `test_continuous_milestone_is_created_without_a_due_on_key` | passing |
| Closed kinds are created open with a date | `test_closed_milestones_are_created_open_with_their_due_date` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1847, this change |
| 2 | Failing tests written, test cases designed | yes | test commits before implementation |
| 3 | Suite green, coverage held, docs updated | yes | quick.sh |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Owner re-run + `--check` = 0 |

## Exceptions

None.
