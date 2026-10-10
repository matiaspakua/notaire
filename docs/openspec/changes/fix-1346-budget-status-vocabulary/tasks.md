# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1346 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/1346_budget_status_vocabulary` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Backend tests (enum missing), `budget-status.test.ts` (module missing) and TS-0115 (2 failed) observed failing before the change

## 4. Implementación

- [x] 4.1 Minimal implementation

## 5. Actualizar tests existentes

- [x] 5.1 Existing tests adapted without weakening assertions

## 6. Ejecutar regresión

- [x] 6.1 Backend `mvn test`, `bash frontend/verify.sh` green; Playwright chromium suite against the branch build and backend

## 7. Ejecutar Playwright

- [x] 7.1 TS-0115, TS-0113, TS-0010, presupuesto-plantilla, presupuesto-catalogo-items plus the chromium suite

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body ends with `Closes #1346`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close issue

## Definition of Done

- [ ] All gates passed
