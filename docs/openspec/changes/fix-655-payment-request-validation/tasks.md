# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #655 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/655_payment_request_validation` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 6 new `PaymentControllerTest` cases observed failing before the change

## 4. Implementación

- [x] 4.1 Minimal implementation

## 5. Actualizar tests existentes

- [x] 5.1 Existing tests adapted without weakening assertions

## 6. Ejecutar regresión

- [x] 6.1 payment unit/integration tests and Bruno payments (15 requests, 26 tests) green; backend full suite run before push; run_pipeline.sh not run (no Docker)

## 7. Ejecutar Playwright

- [x] 7.1 Not applicable unless UI changes

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)
- [x] 8.2 OpenAPI gate: the 4 accepted breaks listed in `backend-api/openapi/accepted-breaking-changes.txt`; guard tests observed failing first

## 9. Commits atómicos

- [x] 9.1 Conventional Commit ending with `Closes #655`

## 10. Pull Request y validación CI

- [x] 10.1 Push and open PR (#1312)
- [ ] 10.2 `bash scripts/run_pipeline.sh` green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test and close issue

## Definition of Done

- [ ] All gates passed
