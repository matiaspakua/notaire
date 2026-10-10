# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1347 | open → in progress |
| Use Case | RF-23 – Saber el estado de un trámite; CU14 – Consultar estado gestión; CU70/CU71 | exists |
| Specification | `docs/openspec/changes/fix-1347-dashboard-latest-case/` | Gate 1 draft |
| Branch | `fix/1347_dashboard_latest_case` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | [#1406](https://github.com/matiaspakua/notaire/pull/1406) | open |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Newest traced case | `testing/e2e/tests/TS-0035-workflow-tracker-visualization-feature.spec.ts` | passing |
| Skips cases without a workflow | `frontend/src/tests/unit/dashboard-hero.test.tsx` | passing |
| Empty state | `frontend/src/tests/unit/dashboard-hero.test.tsx` | passing |
| No dead button | `testing/e2e/tests/TS-0035-workflow-tracker-visualization-feature.spec.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1347-dashboard-latest-case` |
| 2 | Failing tests written, test cases designed | yes | `dashboard-hero.test.tsx` (module missing) and TS-0035 (2 failed) observed failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
