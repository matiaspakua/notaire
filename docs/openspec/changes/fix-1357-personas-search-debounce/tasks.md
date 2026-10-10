# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1357 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/1357_personas_search_debounce` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 `personas-search.test.tsx` (5 failed) and TS-0015 `#1357` (17 requests instead of 1) observed failing before the change

## 4. Implementación

- [x] 4.1 Minimal implementation (`useSearchPersonas`, page wiring)

## 5. Actualizar tests existentes

- [x] 5.1 No existing test needed changes

## 6. Ejecutar regresión

- [x] 6.1 `bash frontend/verify.sh` green; Playwright chromium suite against the branch build

## 7. Ejecutar Playwright

- [x] 7.1 TS-0015 and TS-0111 plus the chromium suite against the branch production build

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body ends with `Closes #1357`

## 10. Pull Request y validación CI

- [x] 10.1 Push and open PR (#1437)
- [ ] 10.2 CI green and `workspace/sdlc/check-heavy-ci.sh 1437` exit 0

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close issue

## Definition of Done

- [ ] All gates passed
