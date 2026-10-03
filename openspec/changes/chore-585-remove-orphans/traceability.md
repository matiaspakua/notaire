# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```unknown
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #585 | open → in progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists (#585 already listed) |
| Related | #1191 / PR #1196 (created the orphan guard's context), #1190 | referenced |
| Specification | `openspec/changes/chore-585-remove-orphans/` | Gate 1 approved by Owner |
| Branch | `chore/585_remove_orphans` | created from updated `main` |
| Tasks | `tasks.md` | implementation and docs complete; pipeline, PR, Gates 4-5 pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Pre-migration tree is gone | `scripts/test_repo_hygiene.py` | covered |
| No orphaned script under testing | `scripts/test_testing_standalone.py` | covered |
| Removed cURL scripts are gone | same | covered |
| No live reference to a removed path | same | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `api-test/README.md`, 303-testing README, testing DEFINITION | yes | docs commit |
| `CHANGELOG.md` | yes | docs commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh chore-585-remove-orphans` |
| 2 | Failing tests written, test cases designed | yes | guards failed on main before any deletion (hygiene 1 failure; testing guard 3 failures incl. 9 orphan scripts) |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
