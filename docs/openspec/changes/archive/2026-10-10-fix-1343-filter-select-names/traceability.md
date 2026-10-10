# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1343 | open → in progress |
| Use Case | RNF-07 – Diseño de campos y combos; CU76 | exists |
| Specification | `docs/openspec/changes/fix-1343-filter-select-names/` | Gate 1 draft |
| Branch | `fix/1343_filter_select_names` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Toolbar filters | `testing/e2e/tests/TS-0042-accessibility-search-labels-qa.spec.ts` | passing |
| Conditional selects | `testing/e2e/tests/TS-0042-accessibility-search-labels-qa.spec.ts` | passing |
| No unnamed select | `frontend/src/tests/unit/select-accessible-names.test.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1343-filter-select-names` |
| 2 | Failing tests written, test cases designed | yes | `select-accessible-names.test.ts` (2 failed: 5 unnamed controls, keys missing) and `TS-0042` #1343 block observed failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build; axe 0 button-name/select-name on the five routes |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
