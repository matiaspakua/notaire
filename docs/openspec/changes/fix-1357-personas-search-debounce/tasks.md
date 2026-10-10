# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1357 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/1357_personas_search_debounce` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 personas-search.test.tsx and TS-0015 #1357 observed failing before the change (test commit; Playwright saw 17 requests)

## 4. Implementación

- [x] 4.1 `useSearchPersonas` + page wiring

## 5. Actualizar tests existentes

- [x] 5.1 No existing test needed changes

## 6. Ejecutar regresión

- [x] 6.1 `bash frontend/verify.sh` green; Playwright chromium suite against the branch build

## 7. Ejecutar Playwright

- [x] 7.1 TS-0015 (incl. #1357) and TS-0111 25/25, plus the chromium suite

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 CHANGELOG; spec delta below

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body ends with `Closes #1357`

## 10. Pull Request y validación CI

- [ ] 10.1 CI green and `sdlc/check-heavy-ci.sh` exit 0
