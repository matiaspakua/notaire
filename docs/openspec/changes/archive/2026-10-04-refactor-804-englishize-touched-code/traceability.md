# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
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
| Commits | `0b2b73a` Englishize, follow-ups in PR #1201 | merged |
| Pull Request | #1201 | merged 2026-10-03 |
| CI run | CD green on `main` at `dde755e` (run 37158069392) | passed |
| Merge commit | #1201 merged by cursor[bot] at the Owner's request; verified on `main` | done |
| Release / tag | — | pending |
| Smoke test | CD run green on `main`; tie test, Playwright matchers and OpenAPI verified in the tree | done |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| skip_specs — English invalid-transition message | unit `*Transition*` tests (`is not allowed`) | passing |
| PUT cannot change status (#804) | `ManagementHistorialOrphanWriteIntegrationTest` reject cases | passing |
| Tie-date estado-actual uses later History row | `shouldReturnLaterHistoryRowWhenDatesTie` (seed History) | passing (on `main`) |
| Archive edge shows English error in UI | Playwright TS-0011 / TS-0028 | passing (on `main`) |
| OpenAPI artifact matches export | `bash scripts/export-openapi.sh` + Contract CI | passing (on `main`) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/openapi/openapi.yaml` | yes (PR #1201) | `0b2b73a` |
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
