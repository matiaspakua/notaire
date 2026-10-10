# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1340 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/1340_budgets_deeds_pagination` stacked on #1396 (`fix/1340_gestiones_pagination`)

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 `budgets-deeds-pagination.test.tsx` (7 failed) and `TS-0113` observed failing on the #1396 build observed failing before the change

## 4. Implementación

- [x] 4.1 Minimal implementation

## 5. Actualizar tests existentes

- [x] 5.1 Existing tests adapted without weakening assertions

## 6. Ejecutar regresión

- [x] 6.1 `bash frontend/verify.sh` green; Playwright chromium suite against the branch build

## 7. Ejecutar Playwright

- [x] 7.1 TS-0113 plus the chromium suite against the branch production build

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body ends with `Closes #1340`

## 10. Pull Request y validación CI

- [x] 10.1 Push and open PR (#1397)
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close issue

## Definition of Done

- [ ] All gates passed
