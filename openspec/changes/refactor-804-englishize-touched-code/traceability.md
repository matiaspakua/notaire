# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1203 (follow-up to closed #804) | open |
| Use Case | CU02, CU53, CU16, CU83 | exists |
| Related | #804 / PR #1199 (merged); PR #1201 (this draft) | referenced |
| Specification | `openspec/changes/refactor-804-englishize-touched-code/` | Gate 1 |
| Branch | `cursor/refactor-804-englishize-touched-code-69d3` | active |
| Tasks | `tasks.md` | in progress |
| Commits | pending CI-fix push | pending |
| Pull Request | #1201 | draft |
| CI run | pending after push | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| skip_specs — English invalid-transition message | unit `*Transition*` tests (`is not allowed`) | passing on branch |
| PUT cannot change status (#804) | `ManagementHistorialOrphanWriteIntegrationTest` reject cases | passing |
| Tie-date estado-actual uses later History row | `shouldReturnLaterHistoryRowWhenDatesTie` (seed History) | fixing |
| Archive edge shows English error in UI | Playwright TS-0011 / TS-0028 | fixing |
| OpenAPI artifact matches export | `bash scripts/export-openapi.sh` + Contract CI | fixing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/openapi/openapi.yaml` | yes (this PR) | pending |
| CU docs | n/a | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1203 + this folder + `validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | yes | CI red: integration 400, Playwright Spanish matcher |
| 3 | Suite green, coverage held, docs updated | pending | after push |
| 4 | CI green, review approved, no conflicts | pending | draft #1201 |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
