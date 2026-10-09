# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #655 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/655_put_rejects_incomplete_bodies` from `fix/579_constraint_errors_400_workflow` (#1371, for `RequiredFields`)

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 `IncompletePutBodiesIntegrationTest` observed failing 9/10 before the change; `RequiredFieldsTest` written first
- [x] 3.2 Version addendum: `StaleOrMissingVersionIntegrationTest` + `GlobalExceptionHandlerOptimisticLockTest` red 6/6, then 8/13 for the versioned-PUT contract and the movement; Bruno 4 requests red; Playwright `folio-type-edit-version` red (second PUT 409)

## 4. Implementación

- [x] 4.1 Minimal implementation

## 5. Actualizar tests existentes

- [x] 5.1 Existing tests adapted without weakening assertions

## 6. Ejecutar regresión

- [x] 6.1 backend verify, Bruno and Playwright green; oasdiff clean with the six accepted #655 entries; stale-entry check OK; run_pipeline.sh not run (no Docker)

## 7. Ejecutar Playwright

- [x] 7.1 TS-0022, TS-0021, TS-0012/0031/0032
- [x] 7.2 Version addendum: `folio-type-edit-version`, TS-0024, folios-vinculacion, TS-0012/0031/0032

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commit with `Refs #655`

## 10. Pull Request y validación CI

- [x] 10.1 Push and open PR (#1374)
- [ ] 10.2 `bash workspace/sdlc/run_pipeline.sh` green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close issue

## Definition of Done

- [ ] All gates passed
