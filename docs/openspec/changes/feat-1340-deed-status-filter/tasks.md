# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1340 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `feat/1340_deed_status_filter` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 `DeedStatusFilterIntegrationTest` 4/5 failing before the change

## 4. Implementación

- [x] 4.1 Optional status filter, OpenAPI and generated types regenerated

## 5. Actualizar tests existentes

- [x] 5.1 Bruno deeds requests added; COVERAGE updated

## 6. Ejecutar regresión

- [x] 6.1 `bash backend-api/verify.sh` green; Bruno 360/360 requests, 618/618 tests

## 7. Ejecutar Playwright

- [x] 7.1 Not applicable (no UI change; slice 8b adds the picker e2e)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body says `Refs #1340` (intermediate slice)

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test

## Definition of Done

- [ ] All gates passed
