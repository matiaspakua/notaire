# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1361 exists with acceptance criteria (expected behavior 2) and the Owner decision
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `feat/1361_identification_type_catalog` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 JUnit `IdentificationTypeCatalogIntegrationTest` red (3/4: in-use delete answered 204 and deleted the person)
- [x] 3.2 Vitest `identification-types-admin.test.tsx` red (page missing)
- [x] 3.3 TS-0139 red on the pre-change build (4/5)

## 4. Implementación

- [x] 4.1 Controller guards (in-use 409, duplicate name 409, trimmed name); no cascade from types to people
- [x] 4.2 Admin page, hooks, admin index card, breadcrumb, i18n
- [x] 4.3 OpenAPI and generated types regenerated; 3 allowlist entries removed

## 5. Actualizar tests existentes

- [x] 5.1 `SimpleControllersTest` (new constructor, 409 paths); Bruno identification-types unique names and new 06-09

## 6. Ejecutar regresión

- [x] 6.1 `backend-api/verify.sh`, `frontend/verify.sh`, full Bruno (see PR)

## 7. Ejecutar Playwright

- [x] 7.1 TS-0139 and the full chromium suite (see PR)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body says `Closes #1361` (last slice)

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close #1361

## Definition of Done

- [ ] All gates passed
