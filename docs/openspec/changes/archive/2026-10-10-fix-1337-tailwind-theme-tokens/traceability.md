# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1337 | open → in progress |
| Use Case | RNF-05 – Aspecto visual; RNF-09 – Uso de colores en la GUI | exists |
| Specification | `docs/openspec/changes/fix-1337-tailwind-theme-tokens/` | Gate 1 draft |
| Branch | `fix/1337_tailwind_theme_tokens` | created from updated `main` |
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
| Tokens registered | `frontend/src/tests/unit/theme-css-tokens.test.ts` | passing |
| Primary button | `testing/e2e/tests/TS-0101-design-tokens-applied.spec.ts` | passing |
| Active nav and delete | `testing/e2e/tests/TS-0101-design-tokens-applied.spec.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1337-tailwind-theme-tokens` |
| 2 | Failing tests written, test cases designed | yes | `theme-css-tokens.test.ts` (21 failed) and `TS-0101` (2 failed against the `main` build) observed failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite green against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
