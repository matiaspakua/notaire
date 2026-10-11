# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1366 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `feat/1366_status_badge` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Vitest `status-badge.test.tsx` red (module missing)
- [x] 3.2 Playwright TS-0124 failing on the pre-change build

## 4. Implementación

- [x] 4.1 `lib/status-maps.ts` and `components/shared/StatusBadge.tsx`
- [x] 4.2 Six list pages wired; page-local status helpers and ad-hoc badges removed; `status.folio` messages

## 5. Actualizar tests existentes

- [x] 5.1 None needed (texts unchanged: Utilizado, Observado, Disponible keep their labels)

## 6. Ejecutar regresión

- [x] 6.1 `bash frontend/verify.sh` green; full chromium suite (see PR)

## 7. Ejecutar Playwright

- [x] 7.1 TS-0124 plus specs touching the six pages

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body says `Closes #1366`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test

## Definition of Done

- [ ] All gates passed
