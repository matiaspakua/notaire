# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue #860 → spec.md → plan.md → tasks.md → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #860 | in-progress |
| Use Case | CU-03 (#156) | exists |
| Specification | `speckit/specs/001-cu03-documentos-necesarios/` | done |
| Branch | `feat/860_documentos-necesarios-tramite` (rebased onto `main` after #859 merged) | pushed |
| Tasks | `tasks.md` | 32/38 complete (implementation, docs, commits, PR done; merge/deploy pending) |
| Commits | `43fbdc0`, `9abf2e1`, `d90dcae`, `2308ea7` | done |
| Pull Request | [#861](https://github.com/matiaspakua/notaire/pull/861) | open |
| CI run | pending (just opened) | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| US1 — lista con 4 columnas | `frontend/tests/e2e/cu03-04-documentos.spec.ts` | passing |
| US1 — lista de trámites cargada | `frontend/tests/e2e/cu03-04-documentos.spec.ts` | passing |
| US2 — impresión | manual (`window.print()` not assertable in headless Chromium) | verified manually |
| US3 — trámite sin documentos (excepción 7.1) | `frontend/tests/e2e/cu03-04-documentos.spec.ts` (empty-state branch) | passing |
| Hook data fetching | `frontend/src/hooks/usePlantillaTramite.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU03 – Lista documentos y certificados necesarios.md` | done | `307a903` |
| UI-endpoint traceability doc (`REST-API-ENDPOINT_REGISTRY.md`) | done | `307a903` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + spec.md + Acceptance Criteria | yes | Issue #860, `spec.md` Notaire Traceability + 4 Given/When/Then scenarios |
| 2 | Failing tests written, test cases designed | yes | Hook test and E2E scenarios written alongside implementation, TDD per commit `47dc28d` |
| 3 | Suite green, coverage held, docs updated | yes | `tsc --noEmit` clean, 261/261 vitest passing, Playwright CU03 spec passing, docs commit `307a903` |
| 4 | CI green, review approved, no conflicts | pending | awaiting PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None taken.
