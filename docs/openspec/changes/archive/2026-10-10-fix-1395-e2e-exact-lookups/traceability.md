# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1395 | open → in progress |
| Use Case | CU08 – Verificar testimonio; CU11 – Ingresar para inscripción; CU39 – Cargar ítems desde la plantilla; CU81 – Protocolo auxiliar | exists |
| Specification | `docs/openspec/changes/fix-1395-e2e-exact-lookups/` | Gate 1 draft |
| Branch | `fix/e2e_exact_row_lookup` | created from updated `main` |
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
| Row by exact cell | `testing/e2e/tests/TS-0012-documentacion-testimonio-workflow.spec.ts`, `testing/e2e/tests/TS-0081-protocolo-auxiliar-workflow.spec.ts` | passing |
| Own procedure type | `testing/e2e/tests/presupuesto-plantilla.spec.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `none` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1395-e2e-exact-lookups` |
| 2 | Failing tests written, test cases designed | yes | TS-0012 CU11-GW01/CU08-GW01, TS-0081 golden path and presupuesto-plantilla observed failing (strict mode / wrong option) in the full chromium runs before the change |
| 3 | Suite green, coverage held, docs updated | yes | TS-0012, TS-0081, presupuesto-plantilla 25/25 against the production build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
