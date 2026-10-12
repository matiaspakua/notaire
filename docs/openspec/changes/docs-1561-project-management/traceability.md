# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1561 | open |
| Use Case | RNF-21, RNF-22 | exists |
| Specification | `docs/openspec/changes/docs-1561-project-management/` | written |
| Branch | `cursor/project-management-roadmap-4e9a` | open |
| Tasks | `tasks.md` | in progress |
| Commits | `52c0772190b62f168d36186ec2f79411f34a5c22` policy and tests | in progress |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Security non-functional requirement is a release blocker | `test_security_rnf_is_a_release_blocker` | passing |
| Functional requirement is frontend product scope | `test_functional_requirements_and_use_cases_are_frontend_scope` | passing |
| Critical legacy defect is pulled forward | `test_critical_preempts_legacy_theme` | passing |
| Low priority does not enter an earlier train | `test_low_priority_wins_over_area` | passing |
| Close date selects the era | `test_groups_unmilestoned_history_by_close_date` | passing |
| Requirements stay off the pick queue | `test_catalog_and_epics_are_not_pickable` | passing |
| Cross work blocks both product lanes | `test_cross_and_security_block_the_backend_lane` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/github/PROJECT-MANAGEMENT.md` | yes | pending |
| `docs/github/README.md` | yes | pending |
| `docs/200-architecture/201-SAD/sad.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | yes | pending |
| `.claude/agents/cloud-foreman.md` | yes | pending |
| `CHANGELOG.md` | yes | pending |
| `github-page/app/docs/management/page.tsx` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1561, this change, scenarios above |
| 2 | Failing tests written, test cases designed | yes | import failure, then 29 tests OK |
| 3 | Suite green, coverage held, docs updated | pending | unit tests green; product suite n/a |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

No Constitution §12 exception. GitHub milestone and project writes returned
HTTP 403 for this token. That is recorded as a blocker in the handbook, not as
a skipped gate: the repository change includes the apply script and does not
claim the live board was mutated.
