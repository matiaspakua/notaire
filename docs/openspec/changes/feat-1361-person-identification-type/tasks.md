# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1361 exists with acceptance criteria and the Owner decision
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `feat/1361_person_identification_type` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 JUnit `PersonIdentificationTypeIntegrationTest` red (3/5)
- [x] 3.2 Vitest `personas-identification-type.test.tsx` red (3/3)
- [x] 3.3 TS-0015 `#1361` scenarios red on the pre-change build

## 4. Implementación

- [x] 4.1 PersonController type resolution (400 for an unknown id, DNI by name)
- [x] 4.2 Person form select, list label, hook, i18n
- [x] 4.3 OpenAPI and generated types regenerated; allowlist entry removed

## 5. Actualizar tests existentes

- [x] 5.1 Bruno `people/01` asserts the type; e2e number-field locators use `input-dni`

## 6. Ejecutar regresión

- [x] 6.1 `backend-api/verify.sh`, `frontend/verify.sh`, full Bruno (see PR)

## 7. Ejecutar Playwright

- [x] 7.1 TS-0015 and the full chromium suite (see PR)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body says `Refs #1361` (admin catalog slice follows)

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test; the issue stays open for the admin catalog slice

## Definition of Done

- [ ] All gates passed
