# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1753 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-1753-release-please-version-files/` | drafted |
| Branch | `fix/release-please-followup` | created |
| Tasks | `tasks.md` | in progress |
| Commits | `5a13ff939` test, `ae0180665` fix | done |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | next release-please PR | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| POM XPaths match namespaced POMs | `PomXPathTest` | passing |
| OpenAPI info.version is synced on the release branch | `OpenApiSyncTest` | passing |
| Release-please PR passes | `test_accepts_release_please_pr` | passing |
| Other actions-bot PR still needs approval | `test_rejects_actions_bot_pr_that_is_not_release_please` | passing |
| Human on a release-please branch still needs approval | `test_rejects_release_please_branch_from_a_human` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/RELEASE.md` | yes | docs commit |
| `CHANGELOG.md` | yes | docs commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1753 |
| 2 | Failing tests written | yes | `5a13ff939`: 6 failures, then OK |
| 3 | Suite green, docs updated | yes | quick.sh OK |
| 4 | CI green, review approved, no conflicts | | pending |
| 5 | Deployed, smoke test passed, Issue closed | | pending |

## Exceptions

Issue label writes are dropped for the fleet token (#1551); the Owner adds `in-progress` by hand.
