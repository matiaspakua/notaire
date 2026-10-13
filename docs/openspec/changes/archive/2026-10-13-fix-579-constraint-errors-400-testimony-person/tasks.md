# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #579 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/579_constraint_errors_400_testimony_person` stacked on #1370

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 `TestimonyPersonConstraintErrorsTest` and `TestimonyPersonConstraintErrorsIntegrationTest` observed failing 10/11 before the change

## 4. Implementación

- [x] 4.1 Minimal implementation

## 5. Actualizar tests existentes

- [x] 5.1 Existing tests adapted without weakening assertions

## 6. Ejecutar regresión

- [x] 6.1 backend verify, Bruno and Playwright green; oasdiff reports no breaking change; stale-entry check OK; run_pipeline.sh not run (no Docker)

## 7. Ejecutar Playwright

- [x] 7.1 Not applicable unless UI changes

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commit with `Refs #579`

## 10. Pull Request y validación CI

- [x] 10.1 Push and open PR (#1372)
- [ ] 10.2 `bash workspace/sdlc/run_pipeline.sh` green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close issue

## Definition of Done

- [ ] All gates passed
