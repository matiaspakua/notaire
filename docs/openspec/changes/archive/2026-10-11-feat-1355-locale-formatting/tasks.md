# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1355 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `feat/1355_locale_formatting` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Vitest `use-format.test.tsx` red (module missing)
- [x] 3.2 TS-0040 `#1355` cases failing on the pre-change build

## 4. Implementación

- [x] 4.1 lib/format + useFormat
- [x] 4.2 48 call sites, dashboard date and StatValue moved to the hook

## 5. Actualizar tests existentes

- [x] 5.1 `budget-picker.test.tsx` next-intl mock gains `useLocale`

## 6. Ejecutar regresión

- [x] 6.1 `bash frontend/verify.sh` green; full chromium suite (see PR)

## 7. Ejecutar Playwright

- [x] 7.1 TS-0040

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body says `Closes #1355`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test

## Definition of Done

- [ ] All gates passed
