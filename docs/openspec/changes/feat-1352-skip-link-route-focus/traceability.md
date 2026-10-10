# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1352 | open → in progress |
| Use Case | CU76 – Navegar la aplicación; RNF-10 | exists |
| Specification | `docs/openspec/changes/feat-1352-skip-link-route-focus/` | Gate 1 draft |
| Branch | `feat/1352_skip_link_route_focus` | created from updated `main` |
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
| First Tab on a dashboard page | `testing/e2e/tests/TS-0108-keyboard-navigation.spec.ts`, `frontend/src/tests/unit/dashboard-layout.test.tsx` | passing |
| Activating the skip link | `testing/e2e/tests/TS-0108-keyboard-navigation.spec.ts`, `frontend/src/tests/unit/dashboard-layout.test.tsx` | passing |
| Clicking a sidebar link | `testing/e2e/tests/TS-0108-keyboard-navigation.spec.ts`, `frontend/src/tests/unit/dashboard-layout.test.tsx` | passing |
| First load | `testing/e2e/tests/TS-0108-keyboard-navigation.spec.ts`, `frontend/src/tests/unit/dashboard-layout.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1352-skip-link-route-focus` |
| 2 | Failing tests written, test cases designed | yes | `dashboard-layout.test.tsx` (4 of 5 failed) and TS-0108 (2 of 3 failed) observed failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, which is unavailable on the agent box. Playwright ran against a production build (`next build` standalone on :9090) with the backend from `main` on :8080.
