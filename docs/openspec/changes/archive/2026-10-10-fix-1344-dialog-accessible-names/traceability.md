# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1344 | open → in progress |
| Use Case | RNF-06 – Diseño de ventanas; CU76 | exists |
| Specification | `docs/openspec/changes/fix-1344-dialog-accessible-names/` | Gate 1 draft |
| Branch | `fix/1344_dialog_accessible_names` | created from updated `main` |
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
| Create dialogs | `testing/e2e/tests/TS-0106-dialog-accessible-names.spec.ts` | passing |
| English | `testing/e2e/tests/TS-0106-dialog-accessible-names.spec.ts` | passing |
| Every dialog | `frontend/src/tests/unit/dialog.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1344-dialog-accessible-names` |
| 2 | Failing tests written, test cases designed | yes | `dialog.test.tsx` (6 failed) and `TS-0106` (6 failed) observed failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
