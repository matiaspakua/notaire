# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1340 | open → in progress |
| Use Case | CU07 – Generar testimonio; CU08 – Generar minuta de inscripción; CU87 – Vincular escritura y folio | exists |
| Specification | `docs/openspec/changes/fix-1340-deed-pickers/` | Gate 1 draft |
| Branch | `fix/1340_deed_pickers` | created from updated `main` |
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
| Newest signed deeds on open | `deed-picker.test.tsx`, `TS-0123` | passing |
| Any signed deed by number | `deed-picker.test.tsx`, `TS-0123`, `TS-0031`, `TS-0082` | passing |
| Deed already linked to a folio | `deed-picker.test.tsx`, `TS-0123`, `folios-vinculacion` | passing |
| No whole-table loads | `no-whole-table-loads.test.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `frontend/messages/{es,en}.json` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1340-deed-pickers` |
| 2 | Failing tests written, test cases designed | yes | Vitest red (module missing; apiGetPaged present); TS-0123 3/3 failing on the pre-change build |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; full chromium suite (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and a backend from current `main` plus #1383 on :8080.
