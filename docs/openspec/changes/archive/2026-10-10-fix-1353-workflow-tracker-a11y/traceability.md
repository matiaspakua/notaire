# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1353 | open → in progress |
| Use Case | RF-23 – Saber el estado de un trámite; CU83; CU76 | exists |
| Specification | `docs/openspec/changes/fix-1353-workflow-tracker-a11y/` | Gate 1 draft |
| Branch | `fix/1353_workflow_tracker_a11y` | created from updated `main` |
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
| Steps reachable | `frontend/src/tests/unit/workflow-tracker-a11y.test.tsx` | passing |
| Bounded motion | `frontend/src/tests/unit/workflow-tracker-a11y.test.tsx` | passing |
| Dashboard | `testing/e2e/tests/TS-0035-workflow-tracker-visualization-feature.spec.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1353-workflow-tracker-a11y` |
| 2 | Failing tests written, test cases designed | yes | workflow-tracker-a11y.test.tsx and TS-0035 #1353 cases observed failing before the change (test commit) |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
