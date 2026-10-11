# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1360 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `feat/1360_complete_case_edit` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 JUnit `ManagementCompleteCaseReadIntegrationTest` red (3/3, 405)
- [x] 3.2 Vitest `useManagements.complete-case.test.tsx` red (3/3)
- [x] 3.3 TS-0126 red on the pre-change build (3/3)

## 4. Implementación

- [x] 4.1 `GET /managements/{id}/complete-case`
- [x] 4.2 Hooks, edit dialog, i18n
- [x] 4.3 OpenAPI and generated types regenerated; allowlist updated

## 5. Actualizar tests existentes

- [x] 5.1 Bruno procedure-folders GET/PUT/read-back, teardown seq shifted

## 6. Ejecutar regresión

- [x] 6.1 `backend-api/verify.sh`, `frontend/verify.sh`, full Bruno (see PR)

## 7. Ejecutar Playwright

- [x] 7.1 TS-0126 and related management specs (see PR)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body says `Closes #1360`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close the issue

## Definition of Done

- [ ] All gates passed
