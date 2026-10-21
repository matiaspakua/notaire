# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> Rows below Tasks stay `pending` until the corresponding step actually happens.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1932 | in-progress |
| Use Case | CU78 — Security · CU74 — Performance | exists |
| Specification | `docs/openspec/changes/fix-1932-dast-k6-weekly-red/` | Gate 1 |
| Branch | `fix/1932_dast_k6_weekly_red` | from `origin/main` |
| Tasks | `tasks.md` | groups 1–9 done; 10–12 at PR/merge |
| Commits | see PR | recorded in PR |
| Pull Request | see PR for #1932 | pending |
| CI run | PR checks | pending |
| Merge commit | Owner merge | pending |
| Release / tag | next cut | pending |
| Smoke test | dispatch ZAP + k6 on `main`: both green | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| 10049 is allowlisted | `test_rules_allowlist_ignores_non_storable_content_informational` | passing |
| Allowlist entries reviewed with reason | `test_every_allowlist_entry_is_reviewed_and_has_a_reason` | passing |
| Uploads tolerate artifact quota | `test_report_uploads_do_not_fail_a_scan_on_artifact_quota` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1932, this change |
| 2 | Failing tests written, test cases designed | yes | test commit before implementation |
| 3 | Suite green, coverage held, docs updated | yes | quick.sh |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | ZAP + k6 dispatch |

## Exceptions

None.
