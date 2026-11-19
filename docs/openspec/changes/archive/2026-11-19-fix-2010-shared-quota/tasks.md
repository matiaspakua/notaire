> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2010 and CU78
- [x] 1.2 The per-JVM slice already shipped. This slice is the shared table.
- [x] 1.3 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch `cursor/2010-shared-quota-47dc` from `origin/main` at `7eeeb562`

## 3. Gate 2 — Escribir tests

- [x] 3.1 `ApiQuotaSharedStoreIntegrationTest` written first
- [x] 3.2 It failed to compile: `JpaApiRateWindowStore` was missing

## 4. Implementación

- [x] 4.1 V55 `api_rate_windows`
- [x] 4.2 JPA store with an atomic advance, wired into the filter
- [x] 4.3 In-memory store kept for the unit tests

## 5. Actualizar tests existentes

- [x] 5.1 `ApiRequestQuotaTest` still passes on the in-memory window

## 6. Ejecutar regresión

- [x] 6.1 Quota unit and integration tests
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface. The default ceiling is above E2E traffic.

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Threat model, assessment, program, and ADR-018 status

## 9. Commits atómicos

- [x] 9.1 Conventional commit referencing #2010

## 10. Pull Request y validación CI

- [ ] 10.1 Archive, then open the PR. The issue closes when the shared table is on main.

## 11. Deploy

- [ ] 11.1 Merge when preflight exit 0, required checks are green, and the branch is clean

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Flyway V55 applies with the stack. The issue is finished when the shared counter is on main.

## Definition of Done

- [x] Failing test observed
- [ ] preflight.sh --full exit 0
- [ ] PR open and the shared counter merged
