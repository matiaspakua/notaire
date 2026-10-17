# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1878 | in-progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-1878-harden-pm-scripts/` | Gate 1 |
| Branch | `fix/1878_harden_pm_scripts` | active |
| Tasks | `tasks.md` | in progress |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | `--check` mismatches: 0 (Owner applied) | pending confirm |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Fake GitHub rejects due_on null on whole run | `WholeRunFakeGitHubTests` | pending |
| Check lists description and due drift | `RicherCheckTests` | pending |
| Check lists closed issues with status labels | `RicherCheckTests` | pending |
| Check fails on stale committed snapshot | `RicherCheckTests` | pending |
| Missing project scope aborts before writes | `ScopePreflightTests` | pending |
| HTTP error names step | `StepErrorTests` | pending |
| R6 fails on live-shaped data | `test_board_status_label_coherence` / taxonomy | pending |
| All-scope loader uses GraphQL pagination | `test_board_status_label_coherence` | pending |
| Closed non-Done becomes Done | `test_sync_closed_status_done` | pending |
| View contract documents group expectations | `RoadmapContractTests` | pending |
| Closes blocked on multi_pr change | `test_check_closes_refs_guard` | pending |
| Owner scripts pass bash -n | `test_owner_scripts` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/github/README.md` | pending | |
| `docs/300-development/305-project-management/README.md` | pending | |
| `workspace/sdlc/owner/README.md` | pending | |
| `CHANGELOG.md` | pending | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | proposal/design/spec/tasks |
| 2 | Failing tests written | pending | |
| 3 | Suite green, docs updated | pending | |
| 4 | CI green, review approved | pending | |
| 5 | Deployed, smoke, Issue closed | pending | |

## Exceptions

None. Live Owner `--apply` already completed; this change must not re-run
`--apply` or discard snapshot JSON / `workspace/sdlc/owner/` files.
