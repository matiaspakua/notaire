# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1578 | open → in progress |
| Use Case | CU70 – Administrar workflow (nodos y transiciones) | exists |
| Specification | `docs/openspec/changes/feat-1578-workflow-list-view/` | Gate 1 draft |
| Branch | `feat/1578_workflow_list_view` | created from updated `main` |
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
| Keyboard build | TS-0152 | passing |
| Names | Vitest, TS-0152 | passing |
| Confirm | Vitest | passing |
| Duplicate | Vitest | passing |
| Box name | Vitest, TS-0152 | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1578-workflow-list-view` |
| 2 | Failing tests written, test cases designed | yes | the Vitest files failed before the change (component and outgoing count missing) |
| 3 | Suite green, coverage held, docs updated | yes | quick.sh (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
