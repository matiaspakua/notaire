# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1383 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/1383_history_tie_breaker` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 JUnit unit + H2 integration tests red (3 failing)

## 4. Implementación

- [x] 4.1 `HistoryOrder.CHRONOLOGICAL` used on all five read paths

## 5. Actualizar tests existentes

- [x] 5.1 Bruno `history/05` asserts the order of both endpoints

## 6. Ejecutar regresión

- [x] 6.1 `backend-api/verify.sh`, full Bruno (see PR)

## 7. Ejecutar Playwright

- [x] 7.1 TS-0011 and TS-0028 with `--repeat-each` on the branch backend (see PR)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body says `Fixes #1383`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close the issue

## Definition of Done

- [ ] All gates passed
