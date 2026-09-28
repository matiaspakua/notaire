# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue #865 → spec.md → plan.md → tasks.md → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #865 | closed |
| Use Case | CU-43 (#196) | exists |
| Specification | `speckit/specs/archive/003-cu43-reingresar-documentacion/` | done |
| Branch | `feat/865_cu43-reingresar-documentacion` | merged, deleted |
| Tasks | `tasks.md` | all done |
| Commits | `83aac83` (backend), `61f4b59` (frontend), `a25e32a` (docs, closes #865) | done |
| Pull Request | [#871](https://github.com/matiaspakua/notaire/pull/871) | merged |
| CI run | all checks green (Playwright E2E, CI Build/Test/Security, PR Validation, Frontend CI) | passed |
| Merge commit | `4ab24f4ad5c4f2b33c392c459bbf150c138df1e3` (squash) | done |
| Release / tag | none cut — continuous deploy off `main`, no CD run triggered for this merge commit specifically | n/a |
| Smoke test | not performed against a deployed environment (repo has no automated post-merge deploy target); same gap already present for CU03/CU10 | not done |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| US1 — trámites con documentación necesaria | `GestionReingresoDocumentacionIntegrationTest#shouldListTramitesWithDocumentacionNecesaria` | passing |
| US1 — gestión inexistente (404) | `GestionReingresoDocumentacionIntegrationTest#shouldReturn404OnGetWhenGestionDoesNotExist` | passing |
| US1 — trámite sin `PlantillaTramite` (lista vacía) | `GestionReingresoDocumentacionIntegrationTest#shouldReturnEmptyDocumentacionWhenNoPlantilla` | passing |
| US2 — reingreso válido crea `DocumentoPresentado` | `GestionReingresoDocumentacionIntegrationTest#shouldReingresarWhenValid` | passing |
| US2 — tipo de documento no forma parte de la plantilla (400) | `GestionReingresoDocumentacionIntegrationTest#shouldReturn400WhenTipoDocumentoNotInPlantilla` | passing |
| US2 — trámite ajeno a la gestión (400) | `GestionReingresoDocumentacionIntegrationTest#shouldReturn400WhenTramiteDoesNotBelongToGestion` | passing |
| US2 — trámite inexistente (404) | `GestionReingresoDocumentacionIntegrationTest#shouldReturn404WhenTramiteDoesNotExist` | passing |
| Frontend hook + E2E | `useReingresoDocumentacion.test.tsx`, `cu43-reingreso-documentacion.spec.ts` | passing (4/4 hook tests, golden+edge E2E) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU43 – Reingresar documentación.md` | yes | docs commit |
| `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` | yes | docs commit |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | yes | docs commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + spec.md + Acceptance Criteria | yes | Issue #865, `spec.md` Notaire Traceability + Given/When/Then scenarios |
| 2 | Failing tests written, test cases designed | yes | Unit tests (`ReingresoDocumentacionServiceTest`) and integration tests (`GestionReingresoDocumentacionIntegrationTest`) written before implementation, TDD; a Postgres-only regression (`GestionReingresoDocumentacionPgIntegrationTest`) was written specifically to catch a NOT NULL gap H2's `ddl-auto=create` cannot enforce, confirmed red before the fix and green after |
| 3 | Suite green, coverage held, docs updated | yes | `mvn test -pl backend-api` 1611/1611, `mvn verify -pl backend-api` BUILD SUCCESS (JaCoCo ratchet floor held), `mvn test -Ppg-integration -Dtest=GestionReingresoDocumentacionPgIntegrationTest` green, `tsc --noEmit` clean, `vitest run` 269/269, `playwright test cu43-reingreso-documentacion.spec.ts` golden+edge passing, responsive smoke at 320/768/1024px, 3 permanent docs updated |
| 4 | CI green, review approved, no conflicts | yes | All PR #871 checks green (Playwright E2E, CI Build/Test/Security, PR Validation, Frontend CI); `mergeStateStatus: CLEAN`; squash-merged as `4ab24f4` |
| 5 | Deployed, smoke test passed, Issue closed | partial | Issue #865 closed by merge; no live-environment smoke test performed — repo has no automated post-merge deploy target (same gap already present for CU03/CU10, see `speckit/EVALUATION.md`'s "Deferred, not silently dropped" section) |

## Exceptions

None taken.
