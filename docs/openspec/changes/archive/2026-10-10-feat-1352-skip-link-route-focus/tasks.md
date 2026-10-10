# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1352 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `feat/1352_skip_link_route_focus` from updated `main` (1712cfb5)

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 `dashboard-layout.test.tsx` (4 of 5 failed) and TS-0108 (2 of 3 failed on the pre-change build) observed failing before the change

## 4. Implementación

- [x] 4.1 SkipLink, `<main id tabIndex>`, page wrapper `data-route-path`, `useRouteFocus`, `navigation.skipToContent` es/en

## 5. Actualizar tests existentes

- [x] 5.1 No existing test needed changes

## 6. Ejecutar regresión

- [x] 6.1 `bash frontend/verify.sh` green (vitest 582)

## 7. Ejecutar Playwright

- [x] 7.1 TS-0108, TS-0041, TS-0045 and the chromium suite against the branch production build

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body ends with `Closes #1352`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green and `workspace/sdlc/check-heavy-ci.sh <pr>` exit 0

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close issue

## Definition of Done

- [ ] All gates passed
