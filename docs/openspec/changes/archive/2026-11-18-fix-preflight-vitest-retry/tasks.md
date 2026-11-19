> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2046 is open and names CU76
- [x] 1.2 This change is the preflight retry only
- [x] 1.3 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch `cursor/preflight-vitest-retry-47dc`

## 3. Gate 2 — Escribir tests

- [x] 3.1 Test written first
- [x] 3.2 It failed: `run_retry()` was absent

## 4. Implementación

- [x] 4.1 Add `run_retry`
- [x] 4.2 Frontend Vitest uses it

## 5. Actualizar tests existentes

- [x] 5.1 No existing test removed

## 6. Ejecutar regresión

- [x] 6.1 Unit test and `bash -n` passed
- [ ] 6.2 `preflight.sh --full` before push

## 7. Ejecutar Playwright

- [x] 7.1 No product UI

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Learning log updated

## 9. Commits atómicos

- [x] 9.1 Conventional commit

## 10. Pull Request y validación CI

- [ ] 10.1 PR opened

## 11. Deploy

- [ ] 11.1 Owner merges

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Do not close #2046

## Definition of Done

- [x] Failing test observed
- [ ] `preflight.sh --full` exit 0
- [ ] PR open
