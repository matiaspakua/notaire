# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1339, #1338 | open → in progress |
| Use Case | RNF-08 – Especificación de campos; RF-06 – Modificar presupuestos; RF-16/RF-17 – Seguimiento de documentación; RF-29 – Modificar escritura; RF-31 – Testimonios | exists |
| Specification | `docs/openspec/changes/fix-1339-calendar-dates/` | Gate 1 draft |
| Branch | `fix/1339_calendar_dates` | created from updated `main` |
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
| Display in any zone | `testing/e2e/tests/TS-0103-calendar-date-timezones.spec.ts` | passing |
| Edit pre-fill and round trip | `testing/e2e/tests/TS-0102-edit-dialog-date-prefill.spec.ts` | passing |
| Helpers | `frontend/src/tests/unit/dates.test.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1339-calendar-dates` |
| 2 | Failing tests written, test cases designed | yes | `dates.test.ts`, `TS-0102` (4 failed) and `TS-0103` (2 failed) observed failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; vitest green in three TZs; Playwright chromium suite green against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
