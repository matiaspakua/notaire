# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1368 | open → in progress |
| Use Case | RNF-05 – Aspecto visual; RNF-03; CU76 | exists |
| Specification | `docs/openspec/changes/fix-1368-motion-tokens/` | Gate 1 draft |
| Branch | `fix/1368_motion_tokens` | created from updated `main` |
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
| Tokens stay in sync | `frontend/src/tests/unit/motion-tokens.test.ts` | passing |
| No ad-hoc durations | `frontend/src/tests/unit/motion-tokens.test.ts` | passing |
| Fast route change | `testing/e2e/tests/TS-0120-motion.spec.ts` | passing |
| Dialog timing | `testing/e2e/tests/TS-0120-motion.spec.ts` | passing |
| Reduced motion | `testing/e2e/tests/TS-0120-motion.spec.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `frontend/src/theme/README.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1368-motion-tokens` |
| 2 | Failing tests written, test cases designed | yes | motion-tokens.test.ts and TS-0120 observed failing before the change (test commit) |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
