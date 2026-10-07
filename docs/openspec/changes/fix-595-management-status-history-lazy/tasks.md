# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #595 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/595_management_status_history_lazy` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 integration test written first and observed failing (collection loaded eagerly)

## 4. Implementación

- [x] 4.1 Minimal implementation

## 5. Actualizar tests existentes

- [x] 5.1 Existing tests adapted without weakening assertions

## 6. Ejecutar regresión

- [x] 6.1 backend suite and Playwright green; run_pipeline.sh not run (no Docker)

## 7. Ejecutar Playwright

- [x] 7.1 TS-0024, TS-0028, TS-0060, TS-0071, TS-0092, carpetas-de-tramite (chromium, 69 passed)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits with `Refs #595` (slice; #595 stays open)

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 `bash workspace/sdlc/run_pipeline.sh` green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close issue

## Definition of Done

- [ ] All gates passed
