# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1250 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/1250_endpoint_reachability_guard` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 guard written first and observed failing (allowlist missing)

## 4. Implementación

- [x] 4.1 Minimal implementation

## 5. Actualizar tests existentes

- [x] 5.1 Existing tests adapted without weakening assertions

## 6. Ejecutar regresión

- [x] 6.1 contracts, workspace, changelog and links guards green; run_pipeline.sh not run (no Docker)

## 7. Ejecutar Playwright

- [x] 7.1 Not applicable unless UI changes

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commit ending with `Closes #1250`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR (awaiting Owner approval)
- [ ] 10.2 `bash workspace/sdlc/run_pipeline.sh` green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close issue

## Definition of Done

- [ ] All gates passed
