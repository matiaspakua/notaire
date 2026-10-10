# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1369 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/1369_touch_targets` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Vitest `button.test.tsx` and TS-0122 red on the pre-change build (commit 47d9189f)

## 4. Implementación

- [x] 4.1 Button `sm`/`icon-sm` coarse-pointer sizes, Pagination select, gap-2

## 5. Actualizar tests existentes

- [x] 5.1 No existing test needed changes

## 6. Ejecutar regresión

- [x] 6.1 `bash frontend/verify.sh` green; full chromium suite (see PR)

## 7. Ejecutar Playwright

- [x] 7.1 TS-0122 plus mobile list specs

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body ends with `Closes #1369`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close issue

## Definition of Done

- [ ] All gates passed
