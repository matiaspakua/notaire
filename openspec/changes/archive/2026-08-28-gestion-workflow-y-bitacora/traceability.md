# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #833 | closed |
| Use Case | CU13 – Ver historial de gestión (#166); CU83 – Definir Workflow de Estados y Transiciones (#451, #453, #454, #455); CU16 – Archivar Gestión (#169, #819) | exists |
| Specification | `openspec/changes/gestion-workflow-y-bitacora/` | done |
| Branch | `feat/833_gestion-workflow-y-bitacora` | created |
| Tasks | `tasks.md` | complete (Groups 1-12) |
| Commits | `897dde7`, `cb420ed`, `76f127c`, `f102d72`, `fdcc7c5`, `88c51aa`, `828370b`, `64af369` | done |
| Pull Request | [#855](https://github.com/matiaspakua/notaire/pull/855) | merged |
| CI run | PR Validation, CI - Build/Test/Security, Frontend CI, Playwright E2E — Full Suite (all `[ok]` on head commit `27ee4bd`) | passed |
| Merge commit | `050fc65b4123f1b650aa576610c4497a677d2997` (now `main` HEAD) | done |
| Release / tag | rolling `main` (no tagged release for this change) | n/a |
| Smoke test | Post-merge CD/E2E coverage reports recorded for 2026-08-27 (commits `e800a3e`, `0695af4`) confirm the deployed stack stayed green after this merge | passed |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Transición válida se aplica | `GestionTransitionServiceTest#shouldApplyValidTransition`, `GestionTransitionControllerIntegrationTest#shouldApplyValidTransition` | passing |
| Transición inválida es rechazada | `GestionTransitionServiceTest#shouldRejectInvalidTransition`, `GestionTransitionControllerIntegrationTest#shouldRejectInvalidTransition` | passing |
| Gestión sin workflow definido rechaza cualquier transición | `GestionTransitionServiceTest#shouldRejectTransitionWhenNoWorkflowDefinition`, `GestionTransitionControllerIntegrationTest#shouldRejectTransitionWhenNoWorkflowDefinition` | passing |
| Alta de gestión registra su estado inicial | `GestionBitacoraServiceTest#shouldRecordHistorialOnCreate` | passing |
| Transición válida registra el nuevo estado | `GestionBitacoraServiceTest#shouldRecordHistorialOnValidTransition` | passing |
| Archivado registra el estado archivado | `GestionBitacoraServiceTest#shouldRecordHistorialOnArchive` | passing |
| Consulta devuelve el historial completo ordenado | `GestionBitacoraServiceTest#shouldReturnOrderedHistorial`, `GestionBitacoraControllerIntegrationTest#shouldReturnOrderedHistorial` | passing |
| Archiving succeeds when the transition to Archivada is valid | `GestionArchiveDebtServiceTest#shouldArchiveWhenTransitionValid` | passing |
| Archiving is rejected when the transition to Archivada is invalid | `GestionArchiveDebtServiceTest#shouldRejectArchiveWhenTransitionInvalid` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU13 – Ver historial de gestión.md` | yes | `828370b` |
| `docs/100-business/102-use-cases/CU16 – Archivar Gestión.md` | yes | `828370b` |
| `docs/100-business/102-use-cases/CU83 – Definir Workflow de Estados y Transiciones.md` | yes | `828370b` |
| `CHANGELOG.md` | yes | `828370b` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | passed | Issue #833, `specs/gestion-workflow-transicion/spec.md`, `specs/gestion-bitacora/spec.md`, `specs/gestion-archive-debt-check/spec.md` |
| 2 | Failing tests written, test cases designed | passed | `897dde7`, `cb420ed` (unit + integration tests before implementation) |
| 3 | Suite green, coverage held, docs updated | passed | `mvn verify -pl backend-api` green, `npx playwright test` 447 passed/39 skipped against the Docker stack, `bash scripts/preflight.sh --fix` PASSED (15 gates), docs updated in `828370b` |
| 4 | CI green, review approved, no conflicts | passed | PR #855 merged, all 4 required workflows `[ok]` on head commit `27ee4bd` |
| 5 | Deployed, smoke test passed, Issue closed | passed | Merge commit `050fc65` on `main`; post-merge CD/E2E reports (`e800a3e`, `0695af4`) green; Issue #833 closed |

## Exceptions

None.
