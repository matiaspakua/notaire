# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue #863 → spec.md → plan.md → tasks.md → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #863 | in-progress |
| Use Case | CU-10 (#163) | exists |
| Specification | `speckit/specs/002-cu10-movimientos-documentacion-entidades-externas/` | done |
| Branch | `feat/863_cu10-movimientos-documentacion-entidades-externas` | pushed |
| Tasks | `tasks.md` | backend, frontend, E2E, docs done |
| Commits | `d689942` (backend), `8cae0af` (frontend + E2E), `b5fbd6c` (docs) | done |
| Pull Request | [#864](https://github.com/matiaspakua/notaire/pull/864) | open, not merged |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| US1 — detalle con nomenclatura catastral | `GestionDocumentosEntidadesExternasIntegrationTest#shouldListDocumentosEntidadesExternas` | passing |
| US1 — gestión inexistente (404) | `GestionDocumentosEntidadesExternasIntegrationTest#shouldReturn404WhenGestionDoesNotExist` | passing |
| US2 — registrar movimiento válido | `GestionDocumentosEntidadesExternasIntegrationTest#shouldRegistrarMovimiento` | passing |
| US2 — documento no pertenece a la gestión (400) | `GestionDocumentosEntidadesExternasIntegrationTest#shouldReturn400WhenDocumentDoesNotBelongToGestion` | passing |
| US2 — documento inexistente (404) | `GestionDocumentosEntidadesExternasIntegrationTest#shouldReturn404WhenDocumentDoesNotExist` | passing |
| US3 — transición automática exitosa | `DocumentoEntidadExternaServiceTest$IntentarCompletarDocumentacionTests#shouldTransitionGestionWhenAllDocumentsDelivered` | passing |
| US3 — workflow no define transición (no falla) | `DocumentoEntidadExternaServiceTest$IntentarCompletarDocumentacionTests#shouldSwallowBusinessValidationExceptionOnAutoTransition`, `GestionDocumentosEntidadesExternasIntegrationTest#shouldMarkAllDocumentsDeliveredWithoutFailingWhenNoWorkflow` | passing |
| Frontend hook + E2E | `useDocumentosEntidadExterna.test.tsx`, `cu10-documentos-entidades-externas.spec.ts` | passing (golden + edge path) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU10 – Registrar movimientos documentación de entidades externas.md` | yes | this commit |
| `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` | yes | this commit |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | yes | this commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + spec.md + Acceptance Criteria | yes | Issue #863, `spec.md` Notaire Traceability + 7 Given/When/Then scenarios |
| 2 | Failing tests written, test cases designed | yes | Unit tests (`DocumentoEntidadExternaServiceTest`) and integration tests (`GestionDocumentosEntidadesExternasIntegrationTest`) written before implementation, TDD |
| 3 | Suite green, coverage held, docs updated | yes | `mvn test -pl backend-api` 1596/1596 passing, `mvn verify -pl backend-api` BUILD SUCCESS, `bash scripts/preflight.sh` PASSED (spotless, checkstyle, spotbugs, backend verify + pg-integration, frontend typecheck/eslint/vitest/build); Playwright `cu10-documentos-entidades-externas.spec.ts` 2/2 passing against the live dev stack; docs updated |
| 4 | CI green, review approved, no conflicts | pending | PR #864 open, awaiting CI + review |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None taken. A first attempt at fixing a transaction-propagation regression
(`GestionTransitionService.transicionar` under `Propagation.REQUIRES_NEW`)
broke `GestionArchiveDebtService.archivar`'s existing flow and was reverted;
the final fix restructures the call site instead (see `plan.md` US3 task
T020) — recorded here per P4, not hidden.

During Playwright verification against the live dev stack (H2 tests alone
did not catch these, per the project's known H2/PostgreSQL schema-drift
risk), two real bugs surfaced and were fixed:
- `DocumentoPresentadoController.toEntity()` left `liberado`, `observado`
  and `reingresado` unset, which is valid under H2 but a NOT NULL
  violation on real PostgreSQL — every `POST /documento-presentado` 500'd.
- The movimiento dialog in `documentos-entidades-externas/page.tsx` had no
  `max-h`/`overflow-y-auto`, so fields below the fold (checkboxes, save
  button) were unreachable on tall forms.
