# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1341 | open → in progress |
| Use Case | RNF-09 – Uso de colores en la GUI; CU76 | exists |
| Specification | `docs/openspec/changes/fix-1341-text-contrast/` | Gate 1 draft |
| Branch | `fix/1341_text_contrast` | created from updated `main` |
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
| Light backgrounds | `frontend/src/tests/unit/token-contrast.test.ts` | passing |
| Pages | `testing/e2e/tests/TS-0107-text-contrast.spec.ts` | passing |
| No light-gray text utilities | `frontend/src/tests/unit/token-contrast.test.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `.claude/rules/ui-ux-design.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1341-text-contrast` |
| 2 | Failing tests written, test cases designed | yes | `token-contrast.test.ts` (28 failed, then 2 error-text cases) and `TS-0107` (9 of 12 failed on the #1376 build) observed failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build; axe color-contrast 476 → 0 nodes (desktop) and 117 → 0 (390px) on 36 routes |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
